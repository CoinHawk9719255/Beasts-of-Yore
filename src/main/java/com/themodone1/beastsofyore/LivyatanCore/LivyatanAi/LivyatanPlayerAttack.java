package com.themodone1.beastsofyore.LivyatanCore.LivyatanAi;


import com.themodone1.beastsofyore.LivyatanCore.Livyatan;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.RandomUtils;

public class LivyatanPlayerAttack {

    private static final float MAX_TURN_PER_TICK = 3.5F;
    private final Livyatan livyatan;
    private final double swimSpeed;
    private int timeTargetOutOfWater = 0;
    private enum HappyTime {CLAUSTROPHOBIA, TAIL_SWIPE, FAKE_FEINT, DASH}
    private int attackCooldown = 0;
    private int attackAnimationTime = 0;
    private HappyTime happyTimePhase = HappyTime.CLAUSTROPHOBIA;
    private  boolean chooseTimeForHappy = true;
    private int whichHappyShall_I_;
    private int attackTimes;
    public boolean sendBack = false;
    public LivyatanPlayerAttack(Livyatan livyatan, double swimSpeed) {
        this.livyatan = livyatan;
        this.swimSpeed = swimSpeed;
    }

    public void tick(LivingEntity target) {
        if (target == null){
            sendBack = false;
            return;
        }
        if (!target.isAlive()) {
            this.livyatan.setTarget(null);
            sendBack = false;
            return;
        }

        if (!target.isInWater()) {
            timeTargetOutOfWater++;
            if (timeTargetOutOfWater > 100) {
                this.livyatan.setTarget(null);
                timeTargetOutOfWater = 0;
                return;
            }
        } else {
            timeTargetOutOfWater = 0;
        }

        // you know its time i start using comments
        //ive gone idk 4 5 years without it
        //maybe i should since its a recurring problem that i cant read my damn code after writing it
        //anyway this is the finding player and moving part

        //finding player
        double player_X = this.livyatan.getTarget().getX();
        double player_Y = this.livyatan.getTarget().getY();
        double player_Z = this.livyatan.getTarget().getZ();

        if (player_X == Double.NaN || player_Y == Double.NaN || player_Z == Double.NaN) {
            this.livyatan.setTarget(null);
            return;
        }

        //move to player

        if (checkIfCanAttack(this.livyatan.getTarget())) {
            if (chooseTimeForHappy) {
               whichHappyShall_I_= RandomUtils.nextInt(0, 4);
               chooseTimeForHappy = false;
               attackTimes = RandomUtils.nextInt(0, 3);
            }

            switch (happyTimePhase){
                //case claustrophovbia
                case CLAUSTROPHOBIA -> {
                    //easiest one here bro just copy and paste the attack maybe theres a way to call it instead but eh oh well
               sendBack = true;                                   //^^^
                    //shit man comments are goated did that comment ^^^ got sendback idea instanlty
                    this.livyatan.onAttack();
                }


                //case tailswipe
                case TAIL_SWIPE -> {

                  //  this.livyatan.colli
                }


                //case fake feint
                case FAKE_FEINT -> {}


                //case dash
                case DASH -> {}
            }

        }else{
            return;
        }





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
                .inflate(7.0, 2.0, 7.0);

        boolean isFree = this.livyatan.getTarget().level().noCollision(this.livyatan.getTarget(),largeEnough);
       return isFree;
    }


}
