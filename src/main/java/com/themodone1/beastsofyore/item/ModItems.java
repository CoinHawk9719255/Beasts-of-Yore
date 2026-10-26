package com.themodone1.beastsofyore.item;


import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.entities.ModEntities;
import com.themodone1.beastsofyore.sounds.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.Tags;
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
    public static final DeferredItem<Item> ARGENTAVIS_FEATHER = ITEMS.registerSimpleItem("argentavis_feather");
    public static final DeferredItem<Item> ARGENTAVIS_EGG = ITEMS.registerSimpleItem("argentavis_egg");
    public static final DeferredItem<Item> LIVYATAN_TOOTH = ITEMS.registerSimpleItem("livyatan_tooth");
   public static final DeferredItem<Item> LIVYATAN_MEAT = ITEMS.registerSimpleItem(
           "livyatan_meat",
           props -> props.food(
                   new FoodProperties.Builder()
                           .nutrition(8)
                           .saturationModifier(0.2f)
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
    public static final DeferredItem<Item> COOKED_LIVYATAN_MEAT = ITEMS.registerSimpleItem(
            "cooked_livyatan_meat",
            props -> props.food(
                            new FoodProperties.Builder()
                                    .nutrition(20)
                                    .saturationModifier(2.5f)
                                    .build())
                    .component(
                            DataComponents.CONSUMABLE,
                            Consumable.builder()
                                    .consumeSeconds(4f)
                                    .animation(ItemUseAnimation.EAT)
                                    .sound(SoundEvents.GENERIC_EAT)
                                    .soundAfterConsume(SoundEvents.GENERIC_EAT)
                                    .hasConsumeParticles(true)
                                    .build()
                    )
    );
    public static final DeferredItem<Item> LIVYATAN_DOOM = ITEMS.registerItem("livyatan_doom",
            properties -> new Item(properties.sword(
                    ModToolTiers.LIVYTAN_TOOTH,7F,-1.0F))

    );
    public static final DeferredItem<Item> ECHOES_OF_THE_DEEP_MUSIC_DISC = ITEMS.registerItem("echoes_of_the_deep_music_disc",
            properties -> new Item(properties.jukeboxPlayable(ModSounds.ECHOES_OF_THE_DEEP_KEY).rarity(Rarity.EPIC).stacksTo(1)));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
