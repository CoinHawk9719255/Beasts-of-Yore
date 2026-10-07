package com.themodone1.beastsofyore.LivyatanCore.LivyatanAi;

import com.themodone1.beastsofyore.LivyatanCore.Livyatan;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class LivyatanYummyTargets extends TargetGoal {
    private final Livyatan livyatan;
    private final Class<?>[] priorityOrder;
    private final double range;
    private int findTargetCooldown = 0;
    private ArrayList<LivingEntity> blacklist;

    public LivyatanYummyTargets(Livyatan livyatan, Class<?>[] priorityOrder, double range) {
        super(livyatan, false);
        this.livyatan = livyatan;
        this.priorityOrder = priorityOrder;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (findTargetCooldown > 0) {
            findTargetCooldown--;
            return false;
        }
        findTargetCooldown = 10;

        LivingEntity found = findBestTarget();
        if (found == null) {
            return false;
        }


        this.livyatan.setTarget(found);
      
        return false;
    }

    private LivingEntity findBestTarget() {
        AABB searchBox = this.livyatan.getBoundingBox().inflate(this.range);

        for (Class<?> type : priorityOrder) {

            if (type == AbstractBoat.class) {
                    List<AbstractBoat> boats = this.livyatan.level().getEntitiesOfClass(AbstractBoat.class, searchBox, b -> b.isAlive() && this.livyatan.getSensing().hasLineOfSight(b));

                if (boats.isEmpty() == false) {
                    this.livyatan.setBoatTarget(boats.get(0));
                    return null;
                }
                continue;
            }



            if (!LivingEntity.class.isAssignableFrom(type)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends LivingEntity> livingType = (Class<? extends LivingEntity>) type;

            List<? extends LivingEntity> candidates = this.livyatan.level().getEntitiesOfClass(
                    livingType,
                    searchBox,
                    e -> isValidCandidate(e)
            );



            if (!candidates.isEmpty()) {
                return this.livyatan.getTarget() != null && candidates.contains(this.livyatan.getTarget())
                        ? this.livyatan.getTarget()
                        : candidates.get(0);
            }
        }

        return null;
    }

    private boolean isValidCandidate(LivingEntity e) {
        if(blacklist.contains(e)){
            return false;
        }
        if (e.isAlive() == false) {
            return false;
        }
        if (e instanceof Player player && (player.isCreative() || player.isSpectator() || player.isInWater() == false)) {
            return false;
        }
        if (e instanceof Player player && (player.isInWater())){
            this.livyatan.setHappyTime(true);
        }
        if (!(e instanceof Player player)){
            this.livyatan.setHappyTime(false);
        }
        if(!checkIfCanAttack(e)){

                blacklist.add(e);

            return false;
        }
        return this.livyatan.getSensing().hasLineOfSight(e);
    }
    public boolean checkIfCanAttack(LivingEntity target) {
        double player_X = this.livyatan.getTarget().getX();
        double player_Y = this.livyatan.getTarget().getY();
        double player_Z = this.livyatan.getTarget().getZ();

        if (player_X == Double.NaN || player_Y == Double.NaN || player_Z == Double.NaN) {
            this.livyatan.setTarget(null);
            return false;
        }

        AABB largeEnough = this.livyatan.getTarget().getBoundingBox()
                .inflate(4.0, 2.0, 4.0);

        boolean isFree = this.livyatan.getTarget().level().noCollision(this.livyatan.getTarget(),largeEnough);
        return isFree;
    }

    public ArrayList<LivingEntity> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(ArrayList<LivingEntity> blacklist) {
        this.blacklist = blacklist;
    }
}
