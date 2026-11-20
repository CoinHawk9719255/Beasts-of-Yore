package com.themodone1.beastsofyore.item.ingame;

import com.themodone1.beastsofyore.entities.LivyatanDoom.ThrownLivyatanDoom;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

public class LivyatanDoomItem extends Item implements ProjectileItem {
    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 13.0F;
    public static final float PROJECTILE_SHOOT_POWER = 3.5F;

    public LivyatanDoomItem(Properties properties) {
        super(properties);
    }
    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)13.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)-1.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 2, false);
    }

    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.TRIDENT;
    }

    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 72000;
    }

    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof Player player){
            int timeHeld = this.getUseDuration(itemStack, entity) - remainingTime;
            if (timeHeld < 10) {
                return false;
            } else {
                if (level instanceof ServerLevel){
                    ServerLevel serverLevel = (ServerLevel) level;
                    itemStack.hurtWithoutBreaking(1, player);
                    ItemStack thrownItemStack = itemStack.consumeAndReturn(1, player);
                    ThrownLivyatanDoom thrownLivyatanDoom = Projectile.spawnProjectileFromRotation(ThrownLivyatanDoom::new, serverLevel, thrownItemStack, player, 0.0F, PROJECTILE_SHOOT_POWER, 0.5F);
                    if (player.hasInfiniteMaterials()) {
                        thrownLivyatanDoom.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    level.playSound((Entity)null, thrownLivyatanDoom, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    return true;
                }else{
                    return false;
                }
            }
        }else{
            return false;
        }
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        //System.out.println("is testing foir using");
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        } else if (EnchantmentHelper.getTridentSpinAttackStrength(itemInHand, player) > 0.0F && !player.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownLivyatanDoom livyatanDoom = new ThrownLivyatanDoom(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
        livyatanDoom.pickup = AbstractArrow.Pickup.ALLOWED;
        return livyatanDoom;
    }

    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_TRIDENT_ACTIONS.contains(itemAbility);
        //return ItemAbilities.DEFAULT_
    }
}
