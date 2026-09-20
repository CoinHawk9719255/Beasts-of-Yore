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

    private final Livyatan livyatan;
    private final double swimSpeed;
    private int timeTargetOutOfWater = 0;
    private enum HappyTime {CLAUSTROPHOBIA, TAIL_SWIPE, FAKE_FEINT, DASH}
    private int attackCooldown = 0;
    private int attackAnimationTime = 0;
    private HappyTime happyTimePhase = HappyTime.CLAUSTROPHOBIA;
    private  boolean chooseTimeForHappy = true;
    private int whichHappyShall_I_;
    private int attackTimes;
    public boolean sendBack = false;
    private float strengthBoost = 0;
    private int strengthCount = 0;
    // --- tail swipe retreat state ---
    private boolean retreating = false;
    private Vec3 retreatPos = null;
    private int retreatTicks = 0;
    private int claus = 0;

    public LivyatanPlayerAttack(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
    }

    public void tick(LivingEntity target) {
        if (target == null){
            sendBack = false;
            resetRetreat();
            return;
        }
        if (!target.isAlive()) {
            this.livyatan.setTarget(null);
            sendBack = false;
            resetRetreat();
            return;
        }

        if (!target.isInWater()) {
            timeTargetOutOfWater++;
            if (timeTargetOutOfWater > 100) {
                this.livyatan.setTarget(null);
                timeTargetOutOfWater = 0;
                resetRetreat();
                return;
            }
        } else {
            timeTargetOutOfWater = 0;
        }



        // you know its time i start using comments
        //ive gone idk 4 5 years without it
        //maybe i should since its a recurring problem that i cant read my damn code after writing it
        //anyway this is the finding player and moving part

        //finding player
        double player_X = this.livyatan.getTarget().getX();
        double player_Y = this.livyatan.getTarget().getY();
        double player_Z = this.livyatan.getTarget().getZ();

        // (Double.NaN == x is always false in java, Double.isNaN is the way to check)
        if (Double.isNaN(player_X) || Double.isNaN(player_Y) || Double.isNaN(player_Z)) {
            this.livyatan.setTarget(null);
            resetRetreat();
            return;
        }

        // retreat phase runs BEFORE checkIfCanAttack on purpose:
        // if the player swims next to a wall mid-retreat, the whale should still finish swimming away
        // instead of freezing in place until the player is in open water again
        if (retreating) {
            tickRetreat(target);
            return;
        }

        //move to player

        if (checkIfCanAttack(this.livyatan.getTarget())) {
            if (chooseTimeForHappy) {
                System.out.println("changing tacits");
                whichHappyShall_I_= RandomUtils.nextInt(0, 4);
                chooseTimeForHappy = false;
                attackTimes = RandomUtils.nextInt(0, 3);
            }

            switch (happyTimePhase){
                //case claustrophovbia
                case CLAUSTROPHOBIA -> {
                    int clausNow = this.livyatan.getAttackCounter();
                    if (clausNow > claus && clausNow == attackTimes){
                        chooseTimeForHappy = true;
                    }
                    claus = this.livyatan.getAttackCounter();
                    int temp1 = RandomUtils.nextInt(0,4);
                    if (temp1 == 1) {
                        strengthCount++;
                        MobEffectInstance strengthBuff = new MobEffectInstance(MobEffects.STRENGTH, 1200, strengthCount, false, false);
                        this.livyatan.addEffect(strengthBuff);
                    }
                    //easiest one here bro just copy and paste the attack maybe theres a way to call it instead but eh oh well
                    sendBack = true;                                   //^^^
                    //shit man comments are goated did that comment ^^^ got sendback idea instanlty
                    //maybe thats what rubber duck debugging is used for
                    this.livyatan.onAttack();

                }


                //case tailswipe
                case TAIL_SWIPE -> {
                    int clausNow = this.livyatan.getAttackCounter();
                    if (clausNow > claus && clausNow == attackTimes){
                        chooseTimeForHappy = true;
                    }
                    // phase 1: charge the player. once the hit lands we flip to phase 2 (tickRetreat)

                    int temp1 = RandomUtils.nextInt(0,3);
                    if (temp1 == 1) {
                        strengthCount++;
                        MobEffectInstance strengthBuff = new MobEffectInstance(MobEffects.STRENGTH, 1200, strengthCount, false, false);
                        this.livyatan.addEffect(strengthBuff);
                    }
                    double distSq = this.livyatan.distanceToSqr(target);
                    boolean canThrust = distSq > 4.0D && target.isInWater();

                    // 180F = always allowed to thrust (this is what your old code effectively did)
                    swimToward(target.position(), 1.0D, canThrust, 180F, MAX_TURN_PER_TICK);

                    if (distSq <= 75){
                        //System.out.println(distSq +"in range broski");

                        if (this.livyatan.level() instanceof ServerLevel serverLevel) {
                            this.livyatan.doHurtTarget(serverLevel, target);
                            this.livyatan.onAttack();

                            MobEffectInstance nausea = new MobEffectInstance(MobEffects.NAUSEA, 120, 255, true, false);

                            target.addEffect(nausea);
                            strengthBoost = strengthBoost + (RandomUtils.nextInt(0, 4)/10);
                        }

                        // hit landed -> turn around and swim off
                        retreatPos = pickRetreatPos(target);
                        retreating = true;
                        retreatTicks = 0;
                        this.livyatan.setRetreating(true);
                    }
                }


                //case fake feint
                case FAKE_FEINT -> {}


                //case dash
                case DASH -> {}
            }

        }else{
            return;
        }





    }

    /** phase 2 of the tail swipe: turn around, swim to the retreat point, then go back to charging */
    private void tickRetreat(LivingEntity target) {
        retreatTicks++;

        // fast turn + always thrusting = it carves a U-turn right after the hit instead of stopping to pivot
        swimToward(retreatPos, 1.0D, true, 180F, RETREAT_TURN_PER_TICK);

        boolean arrived = this.livyatan.position().distanceToSqr(retreatPos) < 9.0D; // within 3 blocks
        if (arrived || retreatTicks > MAX_RETREAT_TICKS) {
            resetRetreat(); // next tick it's back to phase 1 and charges again
        }
    }

    private void resetRetreat() {
        this.livyatan.setRetreating(false);
        retreating = false;
        retreatPos = null;
        retreatTicks = 0;
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