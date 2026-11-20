package com.themodone1.beastsofyore.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class BossMusicSound extends AbstractTickableSoundInstance {

    private static final float FADE_IN = 0.01F;
    private static final float FADE_OUT = 0.03F;
    boolean fade_QUESTIONMARK;


    public BossMusicSound(SoundEvent event,boolean fade_QUESTIONMARK) {
        super(event, SoundSource.HOSTILE, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
        this.fade_QUESTIONMARK = fade_QUESTIONMARK;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            this.stop();
            return;
        }
    if (ClientBossMusic.isRequested(mc.level.getGameTime())) {
        if (!fade_QUESTIONMARK) {
            this.volume = 1.0F;
        }else{
            this.volume = Math.min(1.0F, this.volume + FADE_IN);
        }
      //  System.out.println("volume currently" + this.volume);
       // System.out.println("Game time"+mc.level.getGameTime());
    }else{
       // System.out.println("Game time"+mc.level.getGameTime());
       // System.out.print("LOwering volume ");
        this.volume -= FADE_OUT;
       // System.out.println(volume);
        if (this.volume < 0.0F){
            ClientBossMusic.setSmth(0);
            //System.out.println(this.volume + ":alr time tostop");
            this.volume = 0.0F;
            this.stop();
        }
    }

    }


}