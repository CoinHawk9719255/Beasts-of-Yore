package com.themodone1.beastsofyore.LivyatanAi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class LivyatanBoatAttack {
    private static final float MAX_TURN_PER_TICK = 3.5F;
    private static final double MAX_SPEED = 0.8D;
    private static final int WAIT_DURATION = 22;
    private static final int BREACH_DURATION = 20;

    //public static int breachTimer = 0;
    private final Livyatan livyatan;
    private final double swimSpeed;

    private enum BoatPhase { APPROACH, WAIT, STRIKE }
    private BoatPhase boatPhase = BoatPhase.APPROACH;

    private BlockPos approachTarget;
    private Vec3 boatPosWhenApproachSet;
    private int waitTimer = 0;
    private boolean boatBiteQueued = false;
    private int boatBiteWindup = 0;
    private static boolean breachedWater = false;
    //public static boolean hasStruken = false;
    private boolean hasAttemptedToUnderwhere = false;


    public LivyatanBoatAttack(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
    }

    public void tick(AbstractBoat boat) {
        if (!boat.isAlive()) {
            if (hasAttemptedToUnderwhere && !this.livyatan.hasStruken()) {
                this.livyatan.triggerAnim("breach", "underthere");
                hasAttemptedToUnderwhere = false;
            }
            reset();
            this.livyatan.setBoatTarget(null);
            return;
        }
//        if (!this.livyatan.isInWater()){
//            System.out.println("Yo man ive been acessed im outta water now");
//           breachedWater = true;
//
//        }
//        //test
//        System.out.println("sturken? "+ hasStruken);
//        if (this.livyatan.isInWater() && hasStruken) {
//            System.out.println("going back to normal");
//            this.livyatan.stopTriggeredAnim("underwhere","underwhere");
//
//        }

        boolean moving = isBoatMoving(boat);

        if (moving) {
            if (boatPhase == BoatPhase.WAIT) {
                reset(); // abandon ambush, switch to direct chase
            }
            if (boatPhase != BoatPhase.STRIKE) { // let an in-progress strike finish
                directChaseAndStrike(boat);

                return;
            }
        }

        switch (boatPhase) {
            case APPROACH -> {
//                if (hasStruken) {
//                   // this.livyatan.stopTriggeredAnim("underthere","underthere");
//
//                    hasStruken = false;
//
//                }

                // System.out.println("Yo mate brached water = " + breachedWater);
                // breachedWater = false;
                // if (breachedWater == true && this.livyatan.isInWater()) {
                // breachedWater = false;

                //  }
                if (approachTarget == null || boatDriftedTooFar(boat)) {
                    approachTarget = findApproachPosition(boat.position());
                    boatPosWhenApproachSet = boat.position();
                }

                Vec3 targetPos = new Vec3(approachTarget.getX() + 0.5, approachTarget.getY(), approachTarget.getZ() + 0.5);
                steerToward(targetPos, true);

                double distSq = this.livyatan.position().distanceToSqr(targetPos);
                if (distSq <= 10.0D) {
                   if (this.livyatan.hasStruken() == false) {
                       // System.out.println("MAN I AM GOING INSANE");
                        this.livyatan.triggerAnim("breach", "underwhere");
                        hasAttemptedToUnderwhere = true;
                       // hasAttemptedToUnderwhere = true;
                   }
                    boatPhase = BoatPhase.WAIT;
                    waitTimer = WAIT_DURATION;
                }
            }
            case WAIT -> {
                steerToward(boat.position(), false);


                // this.livyatan.triggerAnim("underwhere","underwhere");
                waitTimer--;
                if (waitTimer <= 0) {
                    boatPhase = BoatPhase.STRIKE;
                    boatBiteQueued = true;
                    boatBiteWindup = 22;
                    this.livyatan.triggerAnim("attack", "bite");
                }
            }
            case STRIKE -> {

                strikeBoat(boat);

            }
        }
    }


    private boolean boatDriftedTooFar(AbstractBoat boat) {
        if (boatPosWhenApproachSet == null) return false;
        return boat.position().distanceToSqr(boatPosWhenApproachSet) > 64.0D; // 8 blocks ish
    }

    private double lastBoatX = Double.NaN;
    private double lastBoatZ = Double.NaN;
    private int boatStillTicks = 0;

    private boolean isBoatMoving(AbstractBoat boat) {
        if (Double.isNaN(lastBoatX)) {
            // first call baseline make sure to calculate properly


            lastBoatX = boat.getX();
            lastBoatZ = boat.getZ();
            boatStillTicks = 30; // treat as "just settled," not "moving"
            return false;
        }

        double dx = boat.getX() - lastBoatX;
        double dz = boat.getZ() - lastBoatZ;
        double distSq = dx * dx + dz * dz;
        lastBoatX = boat.getX();
        lastBoatZ = boat.getZ();

        if (distSq > 0.0001D) {
            boatStillTicks = 0;
            return true;
        }
        boatStillTicks++;
        return boatStillTicks < 30;
    }

    private void directChaseAndStrike(AbstractBoat boat) {
        Vec3 boatPos = boat.position();

        if (boatBiteQueued) {
            //bit windup rise and strike

            double dyStrike = boatPos.y - this.livyatan.getY();
            double vertical = Mth.clamp(dyStrike / 0.2D, 0.1D, ((2+Math.abs((boat.getY() - this.livyatan.getY())) / 3)));

            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().x, vertical, this.livyatan.getDeltaMovement().z);
            steerToward(boatPos, false); // now runs every windup tick, tracking the moving boat
            this.livyatan.triggerAnim("breach", "underwhere");
            boatBiteWindup--;
            if (boatBiteWindup <= 15) {
                double currentDistSq = this.livyatan.position().distanceToSqr(boat.position());
                resolveStrike(boat, currentDistSq <= 150.0D);
            }
            if (boatBiteWindup <= 0) {
                reset();
            }
            return;
        }

        // no biting chasee chasee below booty
        BlockPos shadowTarget = findApproachPosition(boatPos);
        Vec3 shadowPos = new Vec3(shadowTarget.getX() + 0.5, shadowTarget.getY(), shadowTarget.getZ() + 0.5);
        steerToward(shadowPos, true);

        double dx = boatPos.x - this.livyatan.getX();
        double dz = boatPos.z - this.livyatan.getZ();
        double horizontalDistSq = dx * dx + dz * dz;

        if (horizontalDistSq <= 10.0D) {    // ~idk anymore changed it so much blocks horizontally under the boat
            queueBite();
        }
    }
    private void strikeBoat(AbstractBoat boat) {
        // this.livyatan.triggerAnim("underwhere","underwhere");
        double dy = Math.abs(boat.getY() - this.livyatan.getY());
        double vertical = Mth.clamp(dy / 0.2D, 0.1D, ((2+Math.abs((boat.getY() - this.livyatan.getY())) / 3)));
        this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().x, vertical, this.livyatan.getDeltaMovement().z);
        steerToward(boat.position(), false);
        double distanceVertically = Math.abs(boat.getY() - this.livyatan.getY());
        boatBiteWindup--;
        //System.out.println("distance vertically "+distanceVertically);

        if(boatBiteWindup <= 15) {
            resolveStrike(boat,true);

           // breakBoat(boat);
            //this.livyatan.setBreachTimer(BREACH_DURATION);
            //System.out.println("breaking boat at distance"+distanceVertically);
            //this.livyatan.setHasStruken(true);

        }
        if (boatBiteWindup <= 0) {


           // if (distanceVertically <= 20.0D) {
            //    this.livyatan.setHasStruken(true);


           // }
            reset();
        }

    }


    private void steerToward(Vec3 targetPos, boolean thrust) {
        double dx = targetPos.x - this.livyatan.getX();
        double dy = targetPos.y - this.livyatan.getY();
        double dz = targetPos.z - this.livyatan.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float desiredYaw = (float) (Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        float currentYaw = this.livyatan.getYRot();
        float yawDelta = Mth.clamp(Mth.wrapDegrees(desiredYaw - currentYaw), -MAX_TURN_PER_TICK, MAX_TURN_PER_TICK);
        float newYaw = currentYaw + yawDelta;
        this.livyatan.setYRot(newYaw);
        this.livyatan.yBodyRot = newYaw;
        this.livyatan.yHeadRot = newYaw;

        float desiredPitch = (float) -(Mth.atan2(dy, horizontalDistance) * (180D / Math.PI));
        float currentPitch = this.livyatan.getXRot();
        float pitchDelta = Mth.clamp(Mth.wrapDegrees(desiredPitch - currentPitch), -MAX_TURN_PER_TICK, MAX_TURN_PER_TICK);
        this.livyatan.setXRot(currentPitch + pitchDelta);

        if (!thrust) return;

        boolean facingCloseEnough = Math.abs(yawDelta) < MAX_TURN_PER_TICK + 0.1F;
        if (facingCloseEnough && this.livyatan.isInWater()) {
            Vec3 forward = Vec3.directionFromRotation(this.livyatan.getXRot(), this.livyatan.getYRot());
            Vec3 movement = forward.scale(this.swimSpeed * 0.25D);
            double verticalAssist = Mth.clamp(dy * 0.02D, -0.15D, 0.15D);
            movement = movement.add(0, verticalAssist, 0);
            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(movement));

            if (this.livyatan.horizontalCollision) {
                this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(0, 0.1D, 0));
            }

            Vec3 vel = this.livyatan.getDeltaMovement();
            double speedSq = vel.x * vel.x + vel.z * vel.z;
            if (speedSq > MAX_SPEED * MAX_SPEED) {
                double vy = vel.y;
                Vec3 capped = new Vec3(vel.x, 0, vel.z).normalize().scale(MAX_SPEED);
                this.livyatan.setDeltaMovement(capped.x, vy, capped.z);
            }
        }
    }

    private BlockPos findApproachPosition(Vec3 boatPos) {
        if (!(this.livyatan.level() instanceof ServerLevel level)) {
            return BlockPos.containing(boatPos);
        }
        double halfWidth = this.livyatan.getBbWidth() / 2.0D;
        double height = this.livyatan.getBbHeight();

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(
                Mth.floor(boatPos.x), Mth.floor(boatPos.y) - 3, Mth.floor(boatPos.z)
        );

        BlockPos lastValid = checkPos.immutable();

        for (int y = checkPos.getY(); y > level.getMinY(); y--) {
            checkPos.setY(y);

            AABB testBox = new AABB(
                    checkPos.getX() - halfWidth, y, checkPos.getZ() - halfWidth,
                    checkPos.getX() + halfWidth, y + height, checkPos.getZ() + halfWidth
            );

            boolean isWater = level.getFluidState(checkPos).is(net.minecraft.tags.FluidTags.WATER);
            boolean fits = level.noCollision(this.livyatan, testBox);

            if (isWater && fits) {
                lastValid = checkPos.immutable();
            } else {
                break;
            }
        }
        return lastValid;
    }

    private void breakBoat(AbstractBoat boat) {
        //this.livyatan.stopTriggeredAnim("underwhere","underwhere");
        //this.livyatan.triggerAnim("underthere","underthere");
        this.livyatan.swing(InteractionHand.MAIN_HAND);
        boat.ejectPassengers();
        boat.hurt(boat.damageSources().generic(), 5000.0f);
    }
    private void queueBite() {
        if (this.livyatan.hasStruken() == false) {
            this.livyatan.triggerAnim("breach", "underwhere");
            hasAttemptedToUnderwhere = true;
        }
        boatBiteQueued = true;
        boatBiteWindup = 22;
        this.livyatan.triggerAnim("attack", "bite");
    }
    // when strike resolvess hit mis
// breach animation hopefully controlled byu ai astep dont forget and mess it up again
    private boolean strikeResolved = false;

    private void resolveStrike(AbstractBoat boat, boolean hit) {
        if (strikeResolved) return; // guard against repeated calls in the same windup
        strikeResolved = true;
        if (hit) {
            breakBoat(boat);
        }
        this.livyatan.setBreachTimer(BREACH_DURATION);
        this.livyatan.setHasStruken(true);
        hasAttemptedToUnderwhere = false;
    }


    public void reset() {
        boatPhase = BoatPhase.APPROACH;
        approachTarget = null;
        boatPosWhenApproachSet = null;
        waitTimer = 0;
        boatBiteQueued = false;
        boatBiteWindup = 0;
        lastBoatX = Double.NaN;
        lastBoatZ = Double.NaN;
        boatStillTicks = 0;
        strikeResolved = false;
        hasAttemptedToUnderwhere = false;
    }
}