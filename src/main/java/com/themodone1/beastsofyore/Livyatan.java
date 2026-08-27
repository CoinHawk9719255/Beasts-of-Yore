package com.themodone1.beastsofyore;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import com.themodone1.beastsofyore.sounds.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

//import static com.themodone1.beastsofyore.LivyatanBoatAttack.breachTimer;
//import static com.themodone1.beastsofyore.LivyatanBoatAttack.hasStruken;
import static java.lang.IO.print;

public class Livyatan extends WaterAnimal implements GeoEntity {

    private int breachTimer = 0;
    private boolean hasStruken = false;
    //public boolean hasAttemptedToUnderwhere = false;
    public void setBreachTimer(int t) {
        this.breachTimer = t;
    }
    public void setHasStruken(boolean v) { this.hasStruken = v; }
    public boolean hasStruken() { return hasStruken; }
    //public boolean hasAttemptedToUnderwhere() { return hasAttemptedToUnderwhere; }


    int timeToFull20Ticks = 0;
    private static final Class<?>[] yummy_targets_all = { Player.class,  AbstractBoat.class, Turtle.class,Dolphin.class, Nautilus.class, Squid.class, GlowSquid.class, Cod.class, TropicalFish.class, Salmon.class, Drowned.class };


    @Override
    protected void handleAirSupply(ServerLevel level, int preTickAirSupply) {
        if (this.isAlive() && !this.isInWater()) {
            this.setAirSupply(preTickAirSupply - 1);
            if (this.shouldTakeDrowningDamage()) {
                this.setAirSupply(0);
                this.hurtServer(level, this.damageSources().drown(), 2.0F);
            }
        } else {
            this.setAirSupply(3000);
        }

    }

    private AbstractBoat boatTarget;
    private boolean oneBefore = true;
    private boolean oneAfter =true;

    public AbstractBoat getBoatTarget() {
        return boatTarget;
    }

    public void setBoatTarget(AbstractBoat boat) {
        this.boatTarget = boat;
    }

    public Livyatan(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Livyatan>("movement", 0, this::movementController));
        controllers.add(new AnimationController<Livyatan>("attack", 0, state -> PlayState.STOP)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite")));
        controllers.add(new AnimationController<Livyatan>("breach", 0, state -> PlayState.STOP)
                .triggerableAnim("underwhere", RawAnimation.begin().thenPlayAndHold("underwhere"))
                .triggerableAnim("underthere", RawAnimation.begin().thenPlay("underthere")));



    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {

        if (!level().isClientSide()) {
             level().playSound(null, blockPosition(), ModSounds.LIVYATAN_DEATH.value(), SoundSource.HOSTILE,3.0f, 1f);
        }
        return null;

    }

    private <E extends Livyatan> PlayState movementController(AnimationTest<E> state) {
        if (state.isMoving()) {

            return state.setAndContinue(RawAnimation.begin().thenLoop("swim"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));


    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new LivyatanYummyTargets(this, yummy_targets_all, 124.0D));
        this.goalSelector.addGoal(1, new LivyatanAttackGoal(this, 1.7));
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0, 40));
    }
    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 35.0)
                .add(Attributes.ATTACK_KNOCKBACK, 5.5f)
                .add(Attributes.MOVEMENT_SPEED, 1.2)
                .add(Attributes.FOLLOW_RANGE, 124.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100.0D)
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ARMOR,10);

    }



    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean success = super.doHurtTarget(level, target);

        if (success && target instanceof LivingEntity living) {
            living.knockback(0.8F, this.getX() - target.getX(), this.getZ() - target.getZ());

        }

        return success;
    }


    @Override
    public void tick() {

        super.tick();
        oneAfter = isInWater();
        WaterCrash crash_event = new WaterCrash(oneAfter, oneBefore, getX(),getY(),getZ());
        //System.out.println("strueken MY MAN IM GOING INSANE "+hasStruken);
        crash_event.update(level());
        oneBefore = isInWater();
//        if (!isInWater()) {
//            hasStruken = true;
//
//        }

    }


    private static final float  maxTurnPerTickForLivyatanMaybe_I_ShouldMakeTheseVariableNamesLessFlipFloppity = 2.5F;

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getTarget() != null) {
            rotateTowardsTarget(this.getTarget());
        }
        //ANIMATION STOPPER GOD DAMN I HOPE I REMEMBER WHAT I TYPED IT SO I CAN SEARCH FOR IT LATER
        //System.out.println("has struken for reset"+ hasStruken);
       // System.out.println("breach timerr"+breachTimer);
        if (hasStruken) {
            breachTimer--;
            if (breachTimer <= 0) {
                hasStruken = false;
                triggerAnim("breach", "underthere"); // was stopTriggeredAnim("underwhere","underwhere")
               // System.out.println("Resetted");
            }
        }
    }
    private void rotateTowardsTarget(LivingEntity target) {
        double directionX = target.getX() - this.getX();
        double directionY = target.getY() - this.getY();

        float desiredLookAt_I_CantRememeberTheSpecificWord = (float) (Mth.atan2(directionY, directionX) * (180D / Math.PI)) - 90.0F;


        float currentLookingAngle = this.getYRot();
        float shortestDirectionThatIsActualAndNotMathBeingSilly = Mth.wrapDegrees(desiredLookAt_I_CantRememeberTheSpecificWord - currentLookingAngle);
        shortestDirectionThatIsActualAndNotMathBeingSilly = Mth.clamp(shortestDirectionThatIsActualAndNotMathBeingSilly, -maxTurnPerTickForLivyatanMaybe_I_ShouldMakeTheseVariableNamesLessFlipFloppity, maxTurnPerTickForLivyatanMaybe_I_ShouldMakeTheseVariableNamesLessFlipFloppity);


        float newOhYeahItWasCalledYaw = currentLookingAngle + shortestDirectionThatIsActualAndNotMathBeingSilly;



        this.setYRot(newOhYeahItWasCalledYaw);
        this.yBodyRot = newOhYeahItWasCalledYaw;
        this.yHeadRot = newOhYeahItWasCalledYaw;


    }
}


