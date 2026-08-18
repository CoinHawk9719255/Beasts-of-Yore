package com.themodone1.beastsofyore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LivyatanBoatAttack extends Goal {
    private final Livyatan livyatan;
    private final double swimSpeed;
    private static final float MAX_TURN_PER_TICK = 3.5F;

    private int attackCooldown = 0;
    private int attackAnimationTime = 0;
    private boolean animationDoneAndCanBite = false;
    private int timeTargetOutOfWater = 0;

    public LivyatanBoatAttack(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Boat boat = this.livyatan.getBoatTarget();
        return boat != null && boat.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        Boat boat = this.livyatan.getBoatTarget();
        if (boat == null || !boat.isAlive()) {
            return;
        }


        if (!boat.isInWater() && !boat.isUnderWater()) {
            timeTargetOutOfWater++;
            if (timeTargetOutOfWater > 100) {
                this.livyatan.setBoatTarget(null);
                timeTargetOutOfWater = 0;
                return;
            }
        } else {
            timeTargetOutOfWater = 0;
        }



        double dx = boat.getX() - this.livyatan.getX();
        double dy = boat.getY() - this.livyatan.getY();
        double dz = boat.getZ() - this.livyatan.getZ();
        double horizontalDistance = Math.sqrt((dx * dx) + (dz * dz));



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

        double distSq = dx * dx + dy * dy + dz * dz;
        boolean facingCloseEnough = Math.abs(yawDelta) < MAX_TURN_PER_TICK + 0.1F || Math.abs(Mth.wrapDegrees(desiredYaw - currentYaw)) < 30F;



        if (facingCloseEnough && distSq > 4.0D && this.livyatan.isInWater()) {
            Vec3 forward = Vec3.directionFromRotation(this.livyatan.getXRot(), this.livyatan.getYRot());
            Vec3 movement = forward.scale(this.swimSpeed * 0.05D);
            double verticalAssist = Mth.clamp(dy * 0.002D, -0.10D, 0.10D);
            movement = movement.add(0, verticalAssist, 0);

            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(movement));
        } else if (!this.livyatan.isInWater()) {
            Vec3 vel = this.livyatan.getDeltaMovement();
            this.livyatan.setDeltaMovement(vel.x * 0.8D, vel.y, vel.z * 0.8D);
        }




        if (animationDoneAndCanBite) {
            attackAnimationTime--;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        double reach = getAttackReach(boat);
        double animationReach = getAttackAnimationStartDistance(boat);
        boolean isFacing = isFacingTarget(boat);




        if (distSq <= animationReach && attackCooldown <= 0 && isFacing && !animationDoneAndCanBite) {
            attackAnimationTime = 22;
            animationDoneAndCanBite = true;
            this.livyatan.triggerAnim("attack", "bite");
        }



        if (distSq <= reach && isFacing && animationDoneAndCanBite && attackAnimationTime <= 0) {
            animationDoneAndCanBite = false;
            attackCooldown = 30;
            this.livyatan.swing(InteractionHand.MAIN_HAND);

            if (this.livyatan.level() instanceof ServerLevel serverLevel) {


                boat.hurtServer(serverLevel, boat.damageSources().mobAttack(this.livyatan), 40.0F);
                boat.ejectPassengers();
            }
        }
    }

    private double getAttackReach(Entity target) {
        double reach = 9.5 + target.getBbWidth() + 2.0D;
        return reach * reach;
    }

    private double getAttackAnimationStartDistance(Entity target) {
        double animReach = 14.5 + target.getBbWidth() + 2.0D;
        return animReach * animReach;
    }

    private boolean isFacingTarget(Entity target) {
        Vec3 lookVec = this.livyatan.getLookAngle().normalize();
        Vec3 toTarget = target.position().subtract(this.livyatan.position()).normalize();
        return lookVec.dot(toTarget) > 0.8D;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}