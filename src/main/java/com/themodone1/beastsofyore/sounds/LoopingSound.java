package com.themodone1.beastsofyore.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class LoopingSound extends AbstractTickableSoundInstance {

    private final Entity entity;

    public LoopingSound(Entity entity, SoundEvent event) {

        super(event, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = true;   // this is the loop flag
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        // the "told to stop" condition
        if (entity.isRemoved() || !shouldKeepPlaying()) {
            this.stop();       // sets stopped = true, sound engine cuts it
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    private boolean shouldKeepPlaying() {
        // e.g. a synced flag on your entity
        return true;
    }
}
