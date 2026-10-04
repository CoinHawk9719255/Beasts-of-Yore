package com.themodone1.beastsofyore.LivyatanCore.LivyatanAi;


import com.themodone1.beastsofyore.LivyatanCore.Livyatan;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.RandomUtils;

public class LivyatanPlayerAttack {

    private static final float MAX_TURN_PER_TICK = 3.5F;

    // --- tail swipe retreat settings ---
    private static final double RETREAT_MIN_DIST = 10.0D;   // blocks away from the player
    private static final double RETREAT_MAX_DIST = 20.0D;
    private static final int MAX_RETREAT_TICKS = 200;       // 10s safety timeout in case it gets stuck on terrain
    private static final float RETREAT_TURN_PER_TICK = 5.5F; // how fast it whips around after a hit (charging still uses MAX_TURN_PER_TICK)

    // --- dash charge settings ---
    // the charge goal is picked ONCE (in beginCharge) and never re-aimed at the player's live
    // position.
    private static final float CHARGE_TURN_PER_TICK = 8.0F;   // fast snap onto the charge line, then basically straight since the goal doesn't move
    private static final double CHARGE_SPEED_MULT = 2.0D;
    private static final double CHARGE_OVERSHOOT = 6.0D;      // blocks past the player's position, so the pass carries all the way through them
    private static final double CHARGE_HIT_RANGE_SQ = 16.0D;  // 4 blocks
    private static final int MAX_CHARGE_TICKS = 60;           // 3s safety timeout

    private final Livyatan livyatan;
    private final double swimSpeed;
    private int timeTargetOutOfWater = 0;
    private enum HappyTime {CLAUSTROPHOBIA, TAIL_SWIPE, FAKE_FEINT, DASH, SCREECH}
    private int attackCooldown = 0;
    private int attackAnimationTime = 0;
    private HappyTime happyTimePhase = HappyTime.CLAUSTROPHOBIA;
    private  boolean chooseTimeForHappy = true;
    private int whichHappyShall_I_;
    private int attackTimes = 0;
    public boolean sendBack = false;
    private float strengthBoost = 0;
    private int strengthCount = 0;

    // --- retreat state (shared by tail-swipe recovery and dash windup) ---
    private enum RetreatPurpose { NONE, TAIL_SWIPE_RECOVER, DASH_WINDUP }
    private boolean retreating = false;
    private Vec3 retreatPos = null;
    private int retreatTicks = 0;
    private RetreatPurpose retreatPurpose = RetreatPurpose.NONE;
    private int claus = 0;

    // --- dash charge state ---
    private boolean charging = false;
    private Vec3 chargeTarget = null;
    private int chargeTicks = 0;
    private boolean chargeHasHit = false;
    private int dashStrikesRemaining = 0;
    private boolean tailSwipeInProgress = false;
    private boolean claustrophobiaInProgress = false;
    private boolean screechInProgress = false;

    public LivyatanPlayerAttack(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
    }

    public void tick(LivingEntity target) {
        if (target == null){
            sendBack = false;
            resetRetreat();
            resetCharge();
            tailSwipeInProgress = false;
            claustrophobiaInProgress = false;
            return;
        }
        if (!target.isAlive()) {
            this.livyatan.setTarget(null);
            sendBack = false;
            resetRetreat();
            resetCharge();
            tailSwipeInProgress = false;
            claustrophobiaInProgress = false;
            return;
        }

        if (!target.isInWater()) {
            timeTargetOutOfWater++;
            if (timeTargetOutOfWater > 100) {
                this.livyatan.setTarget(null);
                timeTargetOutOfWater = 0;
                resetRetreat();
                resetCharge();
                return;
            }
        } else {
            timeTargetOutOfWater = 0;
        }

        //finding player
        double player_X = this.livyatan.getTarget().getX();
        double player_Y = this.livyatan.getTarget().getY();
        double player_Z = this.livyatan.getTarget().getZ();

        if (Double.isNaN(player_X) || Double.isNaN(player_Y) || Double.isNaN(player_Z)) {
            this.livyatan.setTarget(null);
            resetRetreat();
            resetCharge();
            return;
        }

        // retreat phase runs BEFORE checkIfCanAttack on purpose:
        // if the player swims next to a wall mid-retreat, the whale should still finish swimming away
        // instead of freezing in place until the player is in open water again
        if (retreating) {
            tickRetreat(target);
            return;
        }

        // dash charge phase also skips checkIfCanAttack for the same reason - once it has
        // committed to a straight pass it should carry through, not freeze mid-charge
        if (charging) {
            tickCharge(target);
            return;
        }

        //move to player

        if (checkIfCanAttack(this.livyatan.getTarget())) {


            switch (happyTimePhase){
                //case claustrophovbia
                case CLAUSTROPHOBIA -> {
                    // pick/reroll only once per flurry, same reasoning as TAIL_SWIPE - then
                    // count attackTimes down locally so the flurry actually runs its full length
                    if (!claustrophobiaInProgress) {
                        tacticChanger();
                        claustrophobiaInProgress = true;
                    }
                    strengthBooster();
                    sendBack = true;
                    this.livyatan.onAttack();

                    attackTimes--;
                    if (attackTimes <= 0) {
                        claustrophobiaInProgress = false; // free to pick a new tactic next tick
                    }

                }


                //case tailswipe
                case TAIL_SWIPE -> {
                    // only reroll/decrement attackTimes on the FIRST tick of this attempt - not
                    // every tick of the approach, or it rerolls to a new phase long before the
                    // whale closes the distance to attack range
                    if (!tailSwipeInProgress) {
                        tacticChanger();
                        tailSwipeInProgress = true;
                    }
                    // phase 1: charge the player. once the hit lands we flip to phase 2 (tickRetreat)


                    double distSq = this.livyatan.distanceToSqr(target);
                    boolean canThrust = distSq > 4.0D && target.isInWater();

                    // 180F = always allowed to thrust (this is what your old code effectively did)
                    swimToward(target.position(), 1.0D, canThrust, 180F, MAX_TURN_PER_TICK);

                    if (distSq <= 75){

                        if (this.livyatan.level() instanceof ServerLevel serverLevel) {
                            this.livyatan.doHurtTarget(serverLevel, target);
                            this.livyatan.onAttack();

                            MobEffectInstance nausea = new MobEffectInstance(MobEffects.NAUSEA, 120, 255, true, false);

                            target.addEffect(nausea);
                            strengthBooster();
                        }

                        // hit landed -> turn around and swim off, and clear the guard so the
                        // next time TAIL_SWIPE comes up it's free to pick/reroll again
                        tailSwipeInProgress = false;
                        retreatPos = pickRetreatPos(target);
                        retreating = true;
                        retreatTicks = 0;
                        retreatPurpose = RetreatPurpose.TAIL_SWIPE_RECOVER;
                        this.livyatan.setRetreating(true);
                    }
                }


                //case fake feint
                case FAKE_FEINT -> {
                    tacticChanger();
                }


                //case dash
                case DASH -> {
                    tacticChanger();
                    // kick off a dash sequence: retreat away first (windup), then beginCharge()
                    // picks up from there once the windup retreat finishes. repeated strikes just
                    // mean dashStrikesRemaining > 1, handled at the end of tickCharge.
                    if (!retreating && !charging) {
                        dashStrikesRemaining = RandomUtils.nextInt(2, 4); // 2-3 passes this sequence
                        retreatPos = pickRetreatPos(target);
                        retreating = true;
                        retreatTicks = 0;
                        retreatPurpose = RetreatPurpose.DASH_WINDUP;
                        this.livyatan.setRetreating(true);
                    }
                }
                case SCREECH -> {
                    if (!screechInProgress) {
                        tacticChanger();
                        screechInProgress = true;
                    }
                    double distSq = this.livyatan.distanceToSqr(target);
                    boolean canThrust = distSq > 4.0D && target.isInWater();
                    swimToward(target.position(), 0.4D, canThrust, 180F, MAX_TURN_PER_TICK);
                    //give a dizzy effect
                    //get time leftand add because fuck you for trying to lilypad it pob
                    boolean hasNauseaAlready = this.livyatan.getTarget().hasEffect(MobEffects.NAUSEA);
                    boolean hasSlownessAlready = this.livyatan.getTarget().hasEffect(MobEffects.SLOWNESS);
                    boolean hasMiningFatigueAlready = this.livyatan.getTarget().hasEffect(MobEffects.MINING_FATIGUE);
                    int nauseaTime = 0;
                    int slownessTime = 0;
                    int miningFatigueTime = 0;
                    if(hasNauseaAlready){
                         nauseaTime  = this.livyatan.getTarget().getEffect(MobEffects.NAUSEA).getDuration();
                    }
                    if(hasSlownessAlready){
                        slownessTime = this.livyatan.getTarget().getEffect(MobEffects.SLOWNESS).getDuration();
                    }
                    if(hasMiningFatigueAlready){
                        miningFatigueTime = this.livyatan.getTarget().getEffect(MobEffects.MINING_FATIGUE).getDuration();
                    }
                    nauseaTime +=300;
                    slownessTime +=300;
                    miningFatigueTime +=300;
                    MobEffectInstance nausea = new MobEffectInstance(MobEffects.NAUSEA, nauseaTime, 255, true, false);
                    MobEffectInstance slowness = new MobEffectInstance(MobEffects.SLOWNESS, slownessTime, 4, true, false);
                    MobEffectInstance miningFatigue = new MobEffectInstance(MobEffects.MINING_FATIGUE, miningFatigueTime, 2, true, false);
                   //give player the effet

                    tailSwipeInProgress = false;

                }
            }

        }else{
            return;
        }





    }
    public void onRegularAttackLanded() {
        if (attackTimes > 0) {
            attackTimes--;
        }
        if (attackTimes <= 0) {
            chooseTimeForHappy = true;
            tacticChanger();
            sendBack = false;
        }
    }
    /** phase 2 of the tail swipe: turn around, swim to the retreat point, then go back to charging */
    public void tacticChanger(){
        System.out.println("tacticChanger" + attackTimes);

        if (attackTimes <= 0){
            chooseTimeForHappy = true;
            if (chooseTimeForHappy) {
                System.out.println("changing tacits");
                whichHappyShall_I_= RandomUtils.nextInt(0, 5);
                happyTimePhase = HappyTime.values()[whichHappyShall_I_];
                chooseTimeForHappy = false;
                if (whichHappyShall_I_ == 0) {
                    attackTimes = RandomUtils.nextInt(3, 6);
                }else if(whichHappyShall_I_ == 4){
                    attackTimes = 1;
                }
                else{
                    attackTimes = RandomUtils.nextInt(1, 3);
                }
                System.out.println("attack type" + happyTimePhase);
                System.out.println("attack times for this new attack type" + attackTimes);
            }

        }
        attackTimes--;
    }
    public void strengthBooster(){
        int temp1 = RandomUtils.nextInt(0,4);
        if (temp1 == 1) {
            strengthCount++;
            MobEffectInstance strengthBuff = new MobEffectInstance(MobEffects.STRENGTH, 1200, strengthCount, false, false);
            this.livyatan.addEffect(strengthBuff);
        }
    }
    private void tickRetreat(LivingEntity target) {
        retreatTicks++;

        // fast turn + always thrusting = it carves a U-turn right after the hit instead of stopping to pivot
        swimToward(retreatPos, 1.0D, true, 180F, RETREAT_TURN_PER_TICK);

        boolean arrived = this.livyatan.position().distanceToSqr(retreatPos) < 9.0D; // within 3 blocks
        if (arrived || retreatTicks > MAX_RETREAT_TICKS) {
            RetreatPurpose finishedPurpose = retreatPurpose;
            resetRetreat();
            if (finishedPurpose == RetreatPurpose.DASH_WINDUP) {
                beginCharge(target);
            }
            // TAIL_SWIPE_RECOVER (or NONE) just falls back to tick()'s normal switch next tick
        }
    }

    private void resetRetreat() {
        this.livyatan.setRetreating(false);
        retreating = false;
        retreatPos = null;
        retreatTicks = 0;
        retreatPurpose = RetreatPurpose.NONE;
    }

    /** starts the actual charge: locks in a point past the player so the pass is a straight line, not a chase */
    private void beginCharge(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(this.livyatan.position());
        double len = Math.max(toTarget.length(), 0.001D);
        Vec3 dir = toTarget.scale(1.0D / len);

        chargeTarget = target.position().add(dir.scale(CHARGE_OVERSHOOT));
        charging = true;
        chargeTicks = 0;
        chargeHasHit = false;
    }

    private void tickCharge(LivingEntity target) {
        chargeTicks++;

        // chargeTarget is fixed for the whole pass - this is what keeps it a straight line
        // instead of orbiting the player the way re-aiming at their live position would.
        swimToward(chargeTarget, CHARGE_SPEED_MULT, true, 180F, CHARGE_TURN_PER_TICK);

        if (!chargeHasHit && this.livyatan.distanceToSqr(target) <= CHARGE_HIT_RANGE_SQ) {
            if (this.livyatan.level() instanceof ServerLevel serverLevel) {
                this.livyatan.doHurtTarget(serverLevel, target);
                this.livyatan.onAttack();
                strengthBooster();
            }
            chargeHasHit = true; // only one hit per pass, even though it phases on through
        }

        boolean arrived = this.livyatan.position().distanceToSqr(chargeTarget) < 9.0D;
        if (arrived || chargeTicks > MAX_CHARGE_TICKS) {
            resetCharge();

            dashStrikesRemaining--;
            if (dashStrikesRemaining > 0) {
                // wind up again for another pass
                retreatPos = pickRetreatPos(target);
                retreating = true;
                retreatTicks = 0;
                retreatPurpose = RetreatPurpose.DASH_WINDUP;
                this.livyatan.setRetreating(true);
            }
            // else: sequence is over, next tick() falls through to the normal switch/tacticChanger
        }
    }

    private void resetCharge() {
        charging = false;
        chargeTarget = null;
        chargeTicks = 0;
        chargeHasHit = false;
    }

    /**
     * Your yaw/pitch/thrust logic, but aimed at any point instead of only the target.
     * maxThrustAngle: only thrust if the whale is within this many degrees of facing the goal (180 = always).
     * maxTurn: max degrees per tick it can turn (yaw and pitch).
     */
    private void swimToward(Vec3 goal, double speedMult, boolean allowThrust, float maxThrustAngle, float maxTurn) {
        double dx = goal.x - this.livyatan.getX();
        double dy = goal.y - this.livyatan.getY();
        double dz = goal.z - this.livyatan.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        // --- Yaw (turn to face goal horizontally), clamped per-tick ---
        float desiredYaw = (float) (Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        float currentYaw = this.livyatan.getYRot();
        float rawYawDelta = Mth.wrapDegrees(desiredYaw - currentYaw);
        float yawDelta = Mth.clamp(rawYawDelta, -maxTurn, maxTurn);
        float newYaw = currentYaw + yawDelta;

        this.livyatan.setYRot(newYaw);
        this.livyatan.yBodyRot = newYaw;
        this.livyatan.yHeadRot = newYaw;

        // --- Pitch (turn to face goal vertically), clamped per-tick ---
        float desiredPitch = (float) -(Mth.atan2(dy, horizontalDistance) * (180D / Math.PI));
        float currentPitch = this.livyatan.getXRot();
        float pitchDelta = Mth.clamp(Mth.wrapDegrees(desiredPitch - currentPitch), -maxTurn, maxTurn);
        this.livyatan.setXRot(currentPitch + pitchDelta);

        // --- Thrust forward toward goal (only while the whale is in water) ---
        if (allowThrust && Math.abs(rawYawDelta) <= maxThrustAngle && this.livyatan.isInWater()) {
            Vec3 forward = Vec3.directionFromRotation(this.livyatan.getXRot(), this.livyatan.getYRot());
            Vec3 movement = forward.scale(this.swimSpeed * 0.05D * speedMult);

            double verticalAssist = Mth.clamp(dy * 0.002D, -0.10D, 0.10D);
            movement = movement.add(0, verticalAssist, 0);

            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(movement));

        } else if (!this.livyatan.isInWater()) {
            // out of water: bleed off horizontal momentum
            Vec3 vel = this.livyatan.getDeltaMovement();
            this.livyatan.setDeltaMovement(vel.x * 0.8D, vel.y, vel.z * 0.8D);
        }

        // --- Nudge upward if stuck against something while swimming ---
        if (this.livyatan.horizontalCollision && this.livyatan.isInWater()) {
            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(0, 0.1D, 0));
        }
    }

    /** picks a water spot 10-20 blocks from the target, roughly on the far side from the whale */
    private Vec3 pickRetreatPos(LivingEntity target) {
        var random = this.livyatan.getRandom();

        // direction from target -> whale (aka "away from the player")
        double awayX = this.livyatan.getX() - target.getX();
        double awayZ = this.livyatan.getZ() - target.getZ();
        double baseAngle = Mth.atan2(awayZ, awayX);

        for (int i = 0; i < 8; i++) {
            // +/- 45 degrees of wiggle so it doesn't always retreat in a dead-straight line
            double angle = baseAngle + Math.toRadians((random.nextDouble() - 0.5D) * 90.0D);
            double dist = RETREAT_MIN_DIST + random.nextDouble() * (RETREAT_MAX_DIST - RETREAT_MIN_DIST);

            double x = target.getX() + Math.cos(angle) * dist;
            double z = target.getZ() + Math.sin(angle) * dist;
            double y = this.livyatan.getY() + (random.nextDouble() - 0.5D) * 4.0D;

            if (this.livyatan.level().getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER)) {
                return new Vec3(x, y, z);
            }
        }

        // fallback: no water found, just head straight away (the timeout ends it if it gets stuck)
        double len = Math.max(Math.sqrt(awayX * awayX + awayZ * awayZ), 0.001D);
        return new Vec3(
                target.getX() + (awayX / len) * RETREAT_MIN_DIST,
                this.livyatan.getY(),
                target.getZ() + (awayZ / len) * RETREAT_MIN_DIST);
    }

    public boolean checkIfCanAttack(LivingEntity target) {
        double player_X = this.livyatan.getTarget().getX();
        double player_Y = this.livyatan.getTarget().getY();
        double player_Z = this.livyatan.getTarget().getZ();

        if (player_X == Double.NaN || player_Y == Double.NaN || player_Z == Double.NaN) {
            this.livyatan.setTarget(null);
            return false;
        }

        AABB largeEnough = this.livyatan.getTarget().getBoundingBox()
                .inflate(4.0, 2.0, 4.0);

        boolean isFree = this.livyatan.getTarget().level().noCollision(this.livyatan.getTarget(),largeEnough);
        return isFree;
    }

    private boolean isFacingTarget(LivingEntity target) {
        Vec3 lookVec = this.livyatan.getLookAngle().normalize();
        Vec3 toTarget = target.position().subtract(this.livyatan.position()).normalize();
        return lookVec.dot(toTarget) > 0.8D;
    }

}