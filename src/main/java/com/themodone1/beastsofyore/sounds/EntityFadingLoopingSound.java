package com.themodone1.beastsofyore.sounds;



import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class EntityFadingLoopingSound extends AbstractTickableSoundInstance {
    private final Entity entity;
    private boolean isFadeOutRequested = false;
    private float fadeSpeed;

    public EntityFadingLoopingSound(SoundEvent soundEvent, SoundSource source, Entity entity, float fadeSpeed){


        super(soundEvent, source, RandomSource.create());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.attenuation = Attenuation.LINEAR;
        this.fadeSpeed = fadeSpeed;

        // Snap to initial position immediately
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        // Stop the sound if the entity is dead, removed, or has finished fading out
        if (this.entity.isRemoved() || this.isStopped()) {
            this.stop();
            return;
        }

        // Update sound position to track the moving entity
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();

        // Handle fade-out logic
        if (isFadeOutRequested) {
            this.volume -= fadeSpeed;
            if (this.volume <= 0.0F) {
                this.volume = 0.0F;
                this.stop();
            }
        }
    }

    public void fadeOut() {
        this.isFadeOutRequested = true;
    }
}