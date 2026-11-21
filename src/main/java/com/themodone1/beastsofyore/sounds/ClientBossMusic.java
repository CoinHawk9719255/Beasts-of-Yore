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
    // two-part track
    private static boolean introPlaying = false;
    private static boolean introDone = false;
    private static long introStartTick = 0;


    private ClientBossMusic() {}

    public static void setSmth(int smth) {
        ClientBossMusic.smth = smth;
    }
    private static void reset() {
        current = null;
        introPlaying = false;
        introDone = false;
    }
    public static void request(Entity source, SoundEvent musicName, Double distance,
                               int leeway, String Sped_I_meanSpecialSounding) {
        Minecraft mc = Minecraft.getInstance();

       // System.out.println("special lenght"+specialLength);
       // leeway = leeway;
        if (mc.player == null) return;
        if (mc.player.distanceToSqr(source) > distance * distance) return;
        if (!mc.player.gameMode().isSurvival()) return;
        long now = source.level().getGameTime();
       // lastRequestTick = source.level().getGameTime();

        // nobody asked recently and nothing is playing -> this is a brand new fight,
        // so throw away any leftover state from the last one
        if (!isRequested(now) && (current == null || !mc.getSoundManager().isActive(current))) {
            reset();
        }
        ClientBossMusic.leeway = leeway;
        lastRequestTick = now;

        if (Sped_I_meanSpecialSounding == null) {
            if (current == null || current.isStopped()) {
                current = new BossMusicSound(musicName, true, true);
                mc.getSoundManager().play(current);
            }
            return;
        }
        if ("Watery Grave".equals(Sped_I_meanSpecialSounding)) {
            if (!introDone) {
                if (!introPlaying) {
                    introPlaying = true;
                    introStartTick = now;
                    // plays once, no loop
                    current = new BossMusicSound(ModSounds.PRE_LIVYATAN_FIGHT.value(), true, false);
                    mc.getSoundManager().play(current);
                } else if (now - introStartTick > 10 && !mc.getSoundManager().isActive(current)) {
                    // the intro really finished -> hand over to the fight track
                    introPlaying = false;
                    introDone = true;
                    current = null;
                }
            }
            if (introDone && (current == null || current.isStopped())) {
                current = new BossMusicSound(musicName, false, true);
                mc.getSoundManager().play(current);
            }

        }
    }

    static boolean isRequested(long now) {
        long gap = now - lastRequestTick;
        return gap >= 0 && gap <= leeway; // negative gap = different world, game time jumped
    }
//        if (Sped_I_meanSpecialSounding == null){
//            if (current == null || current.isStopped()){
//                if(mc.player.gameMode().isSurvival()){
//                    current = new BossMusicSound(musicName,true);
//                    mc.getSoundManager().play(current);
//                }
//            }
//        }else if(Sped_I_meanSpecialSounding.equals("Watery Grave")){
//
//           // System.out.println("watery gravy");
//
//               // System.out.println("currentfruit :" +current+"status:");
//                System.out.println(smth);
//                System.out.println("is active?"+mc.getSoundManager().isActive(current));
//                    // System.out.println("entering the zone");
//                     if(smth <=specialLength){
//                     //    System.out.println("um hello?");
//                         smth ++;
//                         if (current == null || current.isStopped()) {
//                         //    System.out.println("Preening the fighting");
//                             current = new BossMusicSound(ModSounds.PRE_LIVYATAN_FIGHT.value(),true);
//                             mc.getSoundManager().play(current);
//
//                         }
//                     }else {
//                            if(smth == specialLength+1){
//                                mc.getSoundManager().stop();
//                                current = null;
//                                smth ++;
//                            }
//
//                         if (current == null || current.isStopped()) {
//                             if (mc.player.gameMode().isSurvival()) {
//                              //   System.out.println("the fighting");
//                                 current = new BossMusicSound(musicName,false);
//                                 mc.getSoundManager().play(current);
//                             }
//
//                         }
//
//                     }




}
