package com.themodone1.beastsofyore.entities.LivyatanDoom;

import com.themodone1.beastsofyore.entities.ModEntities;
import com.themodone1.beastsofyore.item.ModItems;
import com.themodone1.beastsofyore.renders.ThrownLivyatanDoomRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ThrownLivyatanDoom extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY;
    private static final EntityDataAccessor<Boolean> ID_FOIL;
    private static final float WATER_INERTIA = 0.99F;
    private static final boolean DEFAULT_DEALT_DAMAGE = false;
    private boolean dealtDamage = false;
    public int clientSideReturnSpearTickCount;

    public ThrownLivyatanDoom(EntityType<? extends ThrownLivyatanDoom> type, Level level) {
        super(type, level);
    }

    public ThrownLivyatanDoom(Level level, LivingEntity owner, ItemStack LivyatanDoomItem) {
        super( ModEntities.THROWN_LIVYATAN_DOOM.get()
                , owner, level, LivyatanDoomItem, (ItemStack)null);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(LivyatanDoomItem));
        this.entityData.set(ID_FOIL, LivyatanDoomItem.hasFoil());
    }

    public ThrownLivyatanDoom(Level level, double x, double y, double z, ItemStack LivyatanDoomItem) {
        super( ModEntities.THROWN_LIVYATAN_DOOM.get()
                , x, y, z, level, LivyatanDoomItem, LivyatanDoomItem);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(LivyatanDoomItem));
        this.entityData.set(ID_FOIL, LivyatanDoomItem.hasFoil());
    }

    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(ID_LOYALTY, (byte)0);
        entityData.define(ID_FOIL, false);
    }



    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity currentOwner = this.getOwner();
        int loyalty = (Byte)this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && currentOwner != null) {
            if (!this.isAcceptibleReturnOwner()) {
                Level var4 = this.level();
                if (var4 instanceof ServerLevel) {
                    ServerLevel level = (ServerLevel)var4;
                    if (this.pickup == Pickup.ALLOWED) {
                        this.spawnAtLocation(level, this.getPickupItem(), 0.1F);
                    }
                }

                this.discard();
            } else {
                if (!(currentOwner instanceof Player) && this.position().distanceTo(currentOwner.getEyePosition()) < (double)currentOwner.getBbWidth() + (double)1.0F) {
                    this.discard();
                    return;
                }

                this.setNoPhysics(true);
                Vec3 vec = currentOwner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec.y * 0.015 * (double)loyalty, this.getZ());
                double accel = 0.05 * (double)loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec.normalize().scale(accel)));
                if (this.clientSideReturnSpearTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnSpearTickCount;
            }
        }

        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity currentOwner = this.getOwner();
        return currentOwner != null && currentOwner.isAlive() ? !(currentOwner instanceof ServerPlayer) || !currentOwner.isSpectator() : false;
    }

    public boolean isFoil() {
        return (Boolean)this.entityData.get(ID_FOIL);
    }

    protected @Nullable EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return this.dealtDamage ? null : super.findHitEntity(from, to);
    }

    protected Collection<EntityHitResult> findHitEntities(Vec3 from, Vec3 to) {
        EntityHitResult e = this.findHitEntity(from, to);
        return e != null ? List.of(e) : List.of();
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        float dmg = 13.0F;
        Entity currentOwner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, (Entity)(currentOwner == null ? this : currentOwner));
        Level var7 = this.level();
        if (var7 instanceof ServerLevel serverLevel) {
            dmg = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, dmg);
        }

        this.dealtDamage = true;
        if (entity.hurtOrSimulate(damageSource, dmg)) {
            if (entity.is(EntityType.ENDERMAN)) {
                return;
            }

            var7 = this.level();
            if (var7 instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)var7;
                EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(serverLevel, entity, damageSource, this.getWeaponItem(), (weapon) -> this.kill(serverLevel));
            }

            if (entity instanceof LivingEntity) {
                LivingEntity mob = (LivingEntity)entity;
                this.doKnockback(mob, damageSource);
                this.doPostHurtEffects(mob);
            }
        }

        this.deflect(ProjectileDeflection.REVERSE, entity, this.owner, false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    protected void hitBlockEnchantmentEffects(ServerLevel level, BlockHitResult hitResult, ItemStack weapon) {
        Vec3 compensatedHitPosition = hitResult.getBlockPos().clampLocationWithin(hitResult.getLocation());
        Entity var6 = this.getOwner();
        LivingEntity var10002;
        if (var6 instanceof LivingEntity livingOwner) {
            var10002 = livingOwner;
        } else {
            var10002 = null;
        }

        EnchantmentHelper.onHitBlock(level, weapon, var10002, this, (EquipmentSlot)null, compensatedHitPosition, level.getBlockState(hitResult.getBlockPos()), (item) -> this.kill(level));
    }

    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.LIVYATAN_DOOM.get());
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }

    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.dealtDamage = input.getBooleanOr("DealtDamage", false);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(this.getPickupItemStackOrigin()));
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("DealtDamage", this.dealtDamage);
    }

    private byte getLoyaltyFromItem(ItemStack LivyatanDoomItem) {
        Level var3 = this.level();
        byte var10000;
        if (var3 instanceof ServerLevel serverLevel) {
            // this one im not sure what i have to do woth the trident
            var10000 = (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, LivyatanDoomItem, this), 0, 127);
        } else {
            var10000 = 0;
        }

        return var10000;
    }

    public void tickDespawn() {
        int loyalty = (Byte)this.entityData.get(ID_LOYALTY);
        if (this.pickup != Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }

    }

    protected float getWaterInertia() {
        return 0.99F;
    }

    public boolean shouldRender(double camX, double camY, double camZ) {
        return true;
    }

    static {
        ID_LOYALTY = SynchedEntityData.defineId(ThrownLivyatanDoom.class, EntityDataSerializers.BYTE);
        ID_FOIL = SynchedEntityData.defineId(ThrownLivyatanDoom.class, EntityDataSerializers.BOOLEAN);
    }
}
