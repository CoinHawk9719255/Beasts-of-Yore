package com.themodone1.beastsofyore.entities.LivyatanCore;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;


public class LivyatanPart extends PartEntity<Livyatan> {

    public final Livyatan parentMob;
    public final String name;
    private final EntityDimensions size;

    public LivyatanPart(Livyatan parentMob, String name, float w, float h) {
        super(parentMob);
        this.parentMob = parentMob;
        this.name = name;
        this.size = EntityDimensions.scalable(w, h);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return this.isInvulnerableToBase(source) ? false : this.parentMob.hurt(level, this, source, damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {

    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public @Nullable ItemStack getPickResult() {
        return this.parentMob.getPickResult();
    }

    public boolean is(Entity other) {
        return this == other || this.parentMob == other;
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        throw new UnsupportedOperationException();
    }

    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return super.canCollideWith(entity);
    }
}
