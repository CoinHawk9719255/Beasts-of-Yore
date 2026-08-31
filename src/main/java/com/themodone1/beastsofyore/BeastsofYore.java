package com.themodone1.beastsofyore;

import com.themodone1.beastsofyore.entities.ModEntities;
import com.themodone1.beastsofyore.item.ModItems;
import com.themodone1.beastsofyore.LivyatanAi.Livyatan;
import com.themodone1.beastsofyore.sounds.ModSounds;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BeastsofYore.MOD_ID)
public class BeastsofYore {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "beastsofyore";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LIVYATAN.get(), Livyatan.createAttributes().build());
        
    }
    public BeastsofYore(IEventBus modEventBus, ModContainer modContainer) {
        ModSounds.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.register(modEventBus);
        
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::addCreative);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }


    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.MYSTERIOUS_TOOTH);
            event.accept(ModItems.CONCRETION);

        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS){
            event.accept(ModItems.LIVYATAN_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS){
            event.accept(ModItems.LIVYATAN_MEAT);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
