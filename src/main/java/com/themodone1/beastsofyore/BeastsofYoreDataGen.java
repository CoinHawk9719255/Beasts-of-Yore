package com.themodone1.beastsofyore;

import com.themodone1.beastsofyore.datagen.ModModelProvider;
import com.themodone1.beastsofyore.datagen.ModSoundsProvider;
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

        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModSoundsProvider(packOutput));


    }

}
