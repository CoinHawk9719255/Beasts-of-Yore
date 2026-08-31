package com.themodone1.beastsofyore.LivyatanAi;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

public class LivyatanPlayerAttack {

    private final Livyatan livyatan;
    private final double swimSpeed;

    public LivyatanPlayerAttack(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
    }

    public void tick(LivingEntity target) {
        if (!target.isAlive()) {
            this.livyatan.setTarget(null);
        }

    }

}
