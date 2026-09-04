package com.themodone1.beastsofyore.LivyatanCore;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class WaterCrash {
    private boolean moment;
    private boolean oneBefore;
    private double LI_X;
    private double LI_Y;
    private double LI_Z;
   // private Livyatan livyatan;

    public WaterCrash(boolean moment, boolean oneBefore, double LI_X, double LI_Y, double LI_Z) {
        this.moment = moment;
        this.oneBefore = oneBefore;
        this.LI_X = LI_X;
        this.LI_Y = LI_Y;
        this.LI_Z = LI_Z;
       // this.livyatan = livyatan;
    }

    // The logic lives HERE, inside the class
    public void update(Level level) {
        // It can access 'moment' directly because it is inside the class
        if (moment && !oneBefore) {
           // Level level = Minecraft.getInstance().level;
            //System.out.println("Calculating crash physics...");
            if (!level.isClientSide()) {
                //ParticleTypes.SCULK_CHARGE_POP
                //new SimpleParticleType(true)
//                ((ServerLevel) level).addParticle(ParticleTypes.SCULK_CHARGE_POP,
//                        true,
//                        true,
//                        LI_X,
//                        LI_Y,
//                        LI_Z,
//                        5,
//                        5,
//                        5);
                ((ServerLevel) level).sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                        LI_X,LI_Y,LI_Z,
                        50,
                        5,
                        2,
                        5,
                        1.0
                        );
                ((ServerLevel) level).sendParticles(ParticleTypes.SCRAPE,
                        LI_X,LI_Y,LI_Z,
                        50,
                        5,
                        2,
                        5,
                        1.0
                );
                ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD,
                        LI_X,LI_Y,LI_Z,
                        75,
                        5,
                        2,
                        5,
                        1.0
                );
                ((ServerLevel) level).sendParticles(ParticleTypes.GUST_EMITTER_LARGE,
                        LI_X,LI_Y,LI_Z,
                        25,
                        5,
                        2,
                        5,
                        1.0
                );

            }
           // Level#addAlwaysVisibleParticle
            // Add your calculation logic here
            // Example: livyatan.dive();
        } else if (!moment && oneBefore) {
           // System.out.println("No crash moment.");
        }else{

        }
    }
}
