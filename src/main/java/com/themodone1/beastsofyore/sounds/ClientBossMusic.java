package com.themodone1.beastsofyore.sounds;


import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class ClientBossMusic {

    private static BossMusicSound current;
    private static long lastRequestTick = Long.MIN_VALUE;
    private static int leeway = 40;
    private static int smth = 0;
    private static boolean playedIntro = false;
private ClientBossMusic() {}

    public static void setSmth(int smth) {
        ClientBossMusic.smth = smth;
    }

    public static void request(Entity source, SoundEvent musicName, Double distance,
                               int leeway, String Sped_I_meanSpecialSounding, int specialLength) {
        Minecraft mc = Minecraft.getInstance();
        System.out.println("So are we starting over and over or am i just going insane?");
        leeway = leeway;
        if (mc.player == null) return;
        if (mc.player.distanceToSqr(source) > distance * distance) return;
        lastRequestTick = source.level().getGameTime();
        if (Sped_I_meanSpecialSounding == null){
            if (current == null || current.isStopped()){
                if(mc.player.gameMode().isSurvival()){
                    current = new BossMusicSound(musicName,true);
                    mc.getSoundManager().play(current);
                }
            }
        }else if(Sped_I_meanSpecialSounding.equals("Watery Grave")){

            System.out.println("watery gravy");

                System.out.println("currentfruit :" +current+"status:");
                System.out.println(smth);
                     System.out.println("entering the zone");
                     if(smth <=specialLength){
                         System.out.println("um hello?");
                         smth += 1;
                         if (current == null || current.isStopped()) {
                             System.out.println("Preening the fighting");
                             current = new BossMusicSound(ModSounds.PRE_LIVYATAN_FIGHT.value(),true);
                             mc.getSoundManager().play(current);

                         }
                     }else {
                            if(smth == specialLength+1){
                                mc.getSoundManager().stop();
                                current = null;
                                smth ++;
                            }

                         if (current == null || current.isStopped()) {
                             if (mc.player.gameMode().isSurvival()) {
                                 System.out.println("the fighting");
                                 current = new BossMusicSound(musicName,false);
                                 mc.getSoundManager().play(current);
                             }

                         }

                     }

        }
    }
    static boolean isRequested(long now){
    return now - lastRequestTick <= leeway;
    }
}
