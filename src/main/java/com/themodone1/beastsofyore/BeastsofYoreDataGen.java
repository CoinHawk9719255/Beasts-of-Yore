package com.themodone1.beastsofyore;

import com.themodone1.beastsofyore.datagen.*;
import com.themodone1.beastsofyore.sounds.ModSounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = BeastsofYore.MOD_ID)

public class BeastsofYoreDataGen
{
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event)
    {

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput  = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();




        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModSoundsProvider(packOutput));
        generator.addProvider(true,new ModRecipeProvider.Runner(packOutput, lookupProvider));
        generator.addProvider(true, new ModDatapackProvider(packOutput, lookupProvider));
        generator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider, BeastsofYore.MOD_ID));
        generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider, BeastsofYore.MOD_ID));


    }

}
