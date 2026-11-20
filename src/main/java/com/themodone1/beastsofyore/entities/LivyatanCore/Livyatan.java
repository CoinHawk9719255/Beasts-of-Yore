package com.themodone1.beastsofyore.entities.LivyatanCore;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.themodone1.beastsofyore.entities.LivyatanCore.LivyatanAi.LivyatanAttackGoal;
import com.themodone1.beastsofyore.entities.LivyatanCore.LivyatanAi.LivyatanYummyTargets;
import com.themodone1.beastsofyore.sounds.ClientBossMusic;

import com.themodone1.beastsofyore.sounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
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
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;


public class Livyatan extends WaterAnimal implements GeoEntity {


    private int breachTimer = 0;
    private boolean hasStruken = false;
    private boolean happyTime = false;
    public boolean ghoster = false;


    //public boolean hasAttemptedToUnderwhere = false;
    public void setBreachTimer(int t) {
        this.breachTimer = t;
    }

    public void setHasStruken(boolean v) {
        this.hasStruken = v;
    }

    public boolean hasStruken() {
        return hasStruken;
    }

    public void setHappyTime(boolean b) {
        this.happyTime = b;
    }

    public boolean hasHappyTime() {
        return happyTime;
    }

    private int attackCounter = 0;
    private boolean retreating = false;

    public void setRetreating(boolean b) {
        this.retreating = b;
    }

    public boolean isRetreating() {
        return retreating;
    }
//sub entitiing i know im gonna fuck something up BADLY but
// heyt my internet is down so i cant push to github everything else ive donw so oh well

    private final LivyatanPart[] subEntities;
    private final LivyatanPart head;
    private final LivyatanPart body;
    private final LivyatanPart tail;



    public static final int preTickAirSupply = 3000;
    //public boolean hasAttemptedToUnderwhere() { return hasAttemptedToUnderwhere; }

    private static final EntityDataAccessor<Boolean> DATA_IN_COMBAT =
            SynchedEntityData.defineId(Livyatan.class, EntityDataSerializers.BOOLEAN);
    int timeToFull20Ticks = 0;
    private static final Class<?>[] yummy_targets_all = {Player.class, AbstractBoat.class, Turtle.class, Dolphin.class, Nautilus.class, Squid.class, GlowSquid.class, Cod.class, TropicalFish.class, Salmon.class, Drowned.class};

    //music syncing and combating
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IN_COMBAT, false);
    }

    public boolean isInCombat() {
        return this.entityData.get(DATA_IN_COMBAT);
    }

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

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, damage);
    }


    @Override
    public void onAttack() {
        super.onAttack();

        attackCounter++;
    }

    public int getAttackCounter() {
        return attackCounter;
    }

    private AbstractBoat boatTarget;
    private boolean oneBefore = true;
    private boolean oneAfter = true;

    public AbstractBoat getBoatTarget() {
        return boatTarget;
    }

    public void setBoatTarget(AbstractBoat boat) {
        this.boatTarget = boat;
    }

    public Livyatan(EntityType<? extends Livyatan> entityType, Level level) {
        super(entityType, level);


        this.head = new LivyatanPart(this,"head",5.0F,5.0F);
        this.body = new LivyatanPart(this,"body",5.0F,5.0F);
        this.tail = new LivyatanPart(this,"tail",5.0F,5.0F);
        this.subEntities = new LivyatanPart[]{this.head, this.body, this.tail};
        this.setHealth(this.getMaxHealth());
        this.xpReward = 50;
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);


    }

    public void setId(int id) {
        super.setId(id);

        for(int i = 0; i < this.subEntities.length; ++i) {
            this.subEntities[i].setId(id + i + 1);
        }

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
            level().playSound(null, blockPosition(), ModSounds.LIVYATAN_DEATH.value(), SoundSource.HOSTILE, 3.0f, 1f);
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
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
        super.dropFromLootTable(level, source, playerKilled);
    }

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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.ATTACK_KNOCKBACK, 5.5f)
                .add(Attributes.MOVEMENT_SPEED, 1.2)
                .add(Attributes.FOLLOW_RANGE, 124.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100.0D)
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ARMOR, 10);

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
//play boss music
//play boss music

        if (level().isClientSide()) {
           if(isInCombat() && isAlive()){
               ClientBossMusic.request(this, ModSounds.LIVYATAN_FIGHT.value(), 64.0,
                       60,"Watery Grave",
                       280);
           }
        }else{
            this.entityData.set(DATA_IN_COMBAT, getTarget() != null && isAlive()&& getTarget() instanceof Player || getBoatTarget() instanceof AbstractBoat);
        }
        super.tick();

        lilyNoPadders();
        oneAfter = isInWater();
        WaterCrash crash_event = new WaterCrash(oneAfter, oneBefore, getX(), getY(), getZ());
        //System.out.println("strueken MY MAN IM GOING INSANE "+hasStruken);
        crash_event.update(level());
        oneBefore = isInWater();
//        if (!isInWater()) {
//            hasStruken = true;
//
//        }

    }

    private static final float maxTurnPerTickForLivyatanMaybe_I_ShouldMakeTheseVariableNamesLessFlipFloppity = 2.5F;

    @Override
    public void aiStep() {
        if (this.getTarget() == null) retreating = false; // safety so the flag can't get stuck on
        if (this.getTarget() != null && !retreating) {
            rotateTowardsTarget(this.getTarget());
        }
        super.aiStep();
        if (this.getTarget() != null) {
            rotateTowardsTarget(this.getTarget());
        }
        //ANIMATION STOPPER GOD DAMN I HOPE I REMEMBER WHAT I TYPED IT SO I CAN SEARCH FOR IT LATER
        if (hasStruken) {
            breachTimer--;
            if (breachTimer <= 0) {
                hasStruken = false;
                triggerAnim("breach", "underthere"); // was stopTriggeredAnim("underwhere","underwhere")

            }
        }

        // subentities yipeeeeeeee how bad can i be?
        Vec3[] oldPos = new Vec3[this.subEntities.length];
        for(int i = 0; i < this.subEntities.length; ++i) {
            oldPos[i] = new Vec3(this.subEntities[i].getX(), this.subEntities[i].getY(), this.subEntities[i].getZ());
        }
        this.tickPart(this.head,  15.0, 0);
        this.tickPart(this.body,  0.0, 0);
        this.tickPart(this.tail, -15.0, 0);
//        this.tickPart(this.body,0.0,0,0);
//        this.tickPart(this.head,5.0,0,0);
//        this.tickPart(this.tail,-5.0,0,0);

        for(int i = 0; i < this.subEntities.length; ++i) {
            this.subEntities[i].xo = oldPos[i].x;
            this.subEntities[i].yo = oldPos[i].y;
            this.subEntities[i].zo = oldPos[i].z;
            this.subEntities[i].xOld = oldPos[i].x;
            this.subEntities[i].yOld = oldPos[i].y;
            this.subEntities[i].zOld = oldPos[i].z;
        }
        if (!level().isClientSide() && tickCount % 40 == 0) {
            for (LivyatanPart p : subEntities) {
                System.out.println(p.name + " pos=" + p.position() + " box=" + p.getBoundingBox());
            }
        }
        if (level() instanceof ServerLevel sl && tickCount % 10 == 0) {
            for (LivyatanPart p : subEntities) {
                AABB b = p.getBoundingBox();
                for (double x : new double[]{b.minX, b.maxX})
                    for (double y : new double[]{b.minY, b.maxY})
                        for (double z : new double[]{b.minZ, b.maxZ})
                            sl.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    private void tickPart(LivyatanPart part, double forward, double yOffset) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        // Minecraft's forward vector for a yaw is (-sin, cos)
        double dx = -Mth.sin(yawRad) * forward;
        double dz = Mth.cos(yawRad) * forward;
        part.setPos(this.getX() + dx, this.getY() + yOffset, this.getZ() + dz);
    }
    private void rotateTowardsTarget(LivingEntity target) {
        double directionX = target.getX() - this.getX();
        double directionZ = target.getZ() - this.getZ();

        float desiredLookAt_I_CantRememeberTheSpecificWord = (float) (Mth.atan2(directionZ, directionX) * (180D / Math.PI)) - 90.0F;


        float currentLookingAngle = this.getYRot();
        float shortestDirectionThatIsActualAndNotMathBeingSilly = Mth.wrapDegrees(desiredLookAt_I_CantRememeberTheSpecificWord - currentLookingAngle);
        shortestDirectionThatIsActualAndNotMathBeingSilly = Mth.clamp(shortestDirectionThatIsActualAndNotMathBeingSilly, -maxTurnPerTickForLivyatanMaybe_I_ShouldMakeTheseVariableNamesLessFlipFloppity, maxTurnPerTickForLivyatanMaybe_I_ShouldMakeTheseVariableNamesLessFlipFloppity);


        float newOhYeahItWasCalledYaw = currentLookingAngle + shortestDirectionThatIsActualAndNotMathBeingSilly;


        this.setYRot(newOhYeahItWasCalledYaw);
        this.yBodyRot = newOhYeahItWasCalledYaw;
        this.yHeadRot = newOhYeahItWasCalledYaw;


    }

    public boolean lilyNoPadders() {
        Level level = this.level();
        if (level.isClientSide()) return false;

        AABB box = this.getBoundingBox();


        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);

        int targetY = Mth.floor(box.maxY + 1.1D);

        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);


        for (int i = 0; i < 2; i++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, targetY, z);
                    BlockState state = level.getBlockState(pos);


                    if (state.is(Blocks.LILY_PAD)) {
                        level.destroyBlock(pos, true);
                    }
                }
            }
            targetY = Mth.floor(box.minY - 0.1D);
        }
        return false;


    }

    public boolean isMultipartEntity() {
        return true;
    }
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
    }
    public LivyatanPart[] getSubEntities() {
        return this.subEntities;
    }
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }
    public boolean hurt(ServerLevel level, LivyatanPart livyatanPart, DamageSource source, float damage) {
        Level var4 = this.level();
        if (var4 instanceof ServerLevel serverLevel) {
            this.hurtServer(serverLevel, source, damage);
        }
        return false;
    }
}

