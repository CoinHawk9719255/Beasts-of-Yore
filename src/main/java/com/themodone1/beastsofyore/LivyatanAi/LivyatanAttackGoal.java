package com.themodone1.beastsofyore.LivyatanAi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.boat.*;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LivyatanAttackGoal extends Goal {
    private LivyatanBoatAttack boatAttack;
    private LivyatanPlayerAttack playerAttack;
    private int timeSinceLastAttackAnimation = 0;
    private final Livyatan livyatan;
    private final double swimSpeed;
    private static final float MAX_TURN_PER_TICK = 3.5F;
    private int attackCooldown = 0;
    private int attackAnimationTime = 0;
    private int timeTargetOutOfWater = 0;



    private boolean animationDoneAndCanBite = false;
    public LivyatanAttackGoal(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        return (this.livyatan.getTarget() != null && this.livyatan.getTarget().isAlive())
                || this.livyatan.getBoatTarget() != null;

    }

    @Override
    public void tick() {


        LivingEntity target = this.livyatan.getTarget();
        AbstractBoat boat = this.livyatan.getBoatTarget();

        if (this.livyatan.hasHappyTime()){
            //if playerAttack == null{
                playerAttack = new LivyatanPlayerAttack(this.livyatan, this.swimSpeed);
                playerAttack.tick(target);
                return;
           // }
       }
        if (target == null && boat != null) {
            if (boatAttack == null) {
                boatAttack = new LivyatanBoatAttack(this.livyatan, this.swimSpeed);
            }
            boatAttack.tick(boat);
            return;
        }



        if (target == null) {
            if (boatAttack != null) boatAttack.reset();
            return;
        }
       // System.out.println("has attempted to underhwere? haha you said underwhere, SHUT THE FUCK UP " + hasAttemptedToUnderwhere);


        if (!target.isInWater()) {
            timeTargetOutOfWater++;
            if (timeTargetOutOfWater > 100) {
                this.livyatan.setTarget(null);
                timeTargetOutOfWater = 0;
                return;
            }
        } else {
            timeTargetOutOfWater = 0;
        }


        double dx = target.getX() - this.livyatan.getX();
        double dy = target.getY() - this.livyatan.getY();
        double dz = target.getZ() - this.livyatan.getZ();
        double horizontalDistance = Math.sqrt((dx * dx) + (dz * dz));
        double verticalDistance = Math.sqrt((dy * dy) + (dz * dz))-Math.sqrt((dy * dy) + (dx * dx));

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
        float heightDelta = Mth.abs((float) (verticalDistance - this.livyatan.getY()));


        double distSq = dx * dx + dy * dy + dz * dz;
        boolean facingCloseEnough = Math.abs(yawDelta) < MAX_TURN_PER_TICK + 0.1F || Math.abs(Mth.wrapDegrees(desiredYaw - currentYaw)) < 30F;



       // this.livyatan.triggerAnim("breach", "underthere");
//        if (hasAttemptedToUnderwhere){
//            this.livyatan.triggerAnim("breach", "underthere");
//            System.out.println("hold up something seems wrong");
//            this.livyatan.hasStruken();
//        }
        if (facingCloseEnough && distSq > 4.0D && this.livyatan.isInWater() && target.isInWater()) {
            Vec3 forward = Vec3.directionFromRotation(this.livyatan.getXRot(), this.livyatan.getYRot());
            Vec3 movement = forward.scale(this.swimSpeed  * 0.05D);

            double verticalAssist = Mth.clamp(dy * 0.002D, -0.10D, 0.10D);
            movement = movement.add(0, verticalAssist, 0);

            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(movement));

        } else if (!this.livyatan.isInWater()) {
            Vec3 vel = this.livyatan.getDeltaMovement();
            this.livyatan.setDeltaMovement(vel.x * 0.8D, vel.y, vel.z * 0.8D);
        }

        if (this.livyatan.horizontalCollision && this.livyatan.isInWater()) {
            this.livyatan.setDeltaMovement(this.livyatan.getDeltaMovement().add(0, 0.1D, 0));
        }

        if (animationDoneAndCanBite == true){
            attackAnimationTime--;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (attackCooldown == 0){
            timeSinceLastAttackAnimation ++;
        }

        double reach = getAttackReach(target);
        double animationReach = getAttackAnimationStartDistance(target);

        boolean isFacing = isFacingTarget(target);


        if (distSq <= animationReach && attackCooldown <=   0 && isFacing && animationDoneAndCanBite == false) {
            attackAnimationTime = 22;
            animationDoneAndCanBite = true;

            this.livyatan.triggerAnim("attack", "bite");

        }


        if (distSq <= reach  && isFacing && animationDoneAndCanBite == true && attackAnimationTime <= 0) {
            animationDoneAndCanBite = false;
            attackCooldown = 30;
            this.livyatan.swing(InteractionHand.MAIN_HAND);

            if (this.livyatan.level() instanceof ServerLevel serverLevel && attackAnimationTime >= -10 && distSq <= reach) {
                this.livyatan.doHurtTarget(serverLevel, target);
                MobEffectInstance blindness = new MobEffectInstance(MobEffects.BLINDNESS, 60, 5, true, false);
                MobEffectInstance nausea = new MobEffectInstance(MobEffects.NAUSEA, 120, 255, true, false);
                target.addEffect(blindness);
                target.addEffect(nausea);
            }
            timeSinceLastAttackAnimation = 0;
        }


    }


    private double getAttackReach(LivingEntity target) {
        double reach = 9.5 + target.getBbWidth() + 2.0D;
        return reach * reach;
    }
    private double getAttackAnimationStartDistance(LivingEntity target) {
        double Animreach = 15.5 + target.getBbWidth() + 2.0D;
        return Animreach * Animreach;
    }

    private boolean isFacingTarget(LivingEntity target) {
        Vec3 lookVec = this.livyatan.getLookAngle().normalize();
        Vec3 toTarget = target.position().subtract(this.livyatan.position()).normalize();
        return lookVec.dot(toTarget) > 0.8D;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
//        LivingEntity target = this.livyatan.getTarget();
//        Boat boat = this.livyatan.getBoatTarget();
////        if (this.livyatan.tickCount % 5 == 0) {
////            System.out.println("target=" + target + " boat=" + boat);
////        }
        return true;
    }
}



