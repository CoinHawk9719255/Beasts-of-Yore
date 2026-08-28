package com.themodone1.beastsofyore.item;


import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.Livyatan;
import com.themodone1.beastsofyore.entities.ModEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BeastsofYore.MOD_ID);

    public static final DeferredItem<Item> CONCRETION = ITEMS.registerSimpleItem("concretion");
    public static final DeferredItem<Item> MYSTERIOUS_TOOTH = ITEMS.registerSimpleItem("mysterious_tooth");
    public static final DeferredItem<SpawnEggItem> LIVYATAN_SPAWN_EGG = ITEMS.registerItem("livyatan_spawn_egg",
            properties -> new SpawnEggItem(
                    properties.spawnEgg(ModEntities.LIVYATAN.get())
            ));
   // public static final DeferredItem<Item>  = ITEMS.registerSimpleItem("consumable",
            //"livyatan_meat");
   public static final DeferredItem<Item> LIVYATAN_MEAT = ITEMS.registerSimpleItem(
           "livyatan_meat",
           props -> props.food(
                   new FoodProperties.Builder()
                           .nutrition(10)
                           .saturationModifier(0.6f)
                           .build())
                   .component(
                   DataComponents.CONSUMABLE,
                   Consumable.builder()
                           .consumeSeconds(4f)
                           .animation(ItemUseAnimation.EAT)
                           .sound(SoundEvents.GENERIC_EAT)
                           .soundAfterConsume(SoundEvents.GENERIC_EAT)
                           .hasConsumeParticles(true)
                           .onConsume(
                                   new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1), 0.8F)
                           ).onConsume(
                                   new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 200, 2), 0.2F)
                           )
                           .onConsume(
                                   new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, 3), 0.4F)
                           )
                           .build()
           )
   );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
