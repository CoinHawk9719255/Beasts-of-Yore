package com.themodone1.beastsofyore.sounds;

import com.themodone1.beastsofyore.BeastsofYore;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

        // Assuming that your mod id is examplemod
        public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
                DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BeastsofYore.MOD_ID);

        public static void register(IEventBus eventBus) {
                SOUND_EVENTS.register(eventBus);
        }



        // All vanilla sounds use variable range events.
        public static final Holder<SoundEvent> LIVYATAN_DEATH = SOUND_EVENTS.register(
                "livyatan_death",
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, "livyatan_death"))
                // Takes in the registry name
                //SoundEvent::createVariableRangeEvent
        );

        // There is a currently unused method to register fixed range (= non-attenuating) events as well:
//        public static final Holder<SoundEvent> MY_FIXED_SOUND = SOUND_EVENTS.register(
//                "my_fixed_sound",
//                // 16 is the default range of sounds. Be aware that due to OpenAL limitations,
//                // values above 16 have no effect and will be capped to 16.
//                registryName -> SoundEvent.createFixedRangeEvent(registryName, 16f)
//        );
    }

