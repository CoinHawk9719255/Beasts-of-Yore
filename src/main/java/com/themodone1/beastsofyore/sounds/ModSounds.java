package com.themodone1.beastsofyore.sounds;

import com.themodone1.beastsofyore.BeastsofYore;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
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
        public static final Holder<SoundEvent> LIVYATAN_SCREECH = SOUND_EVENTS.register(
                "livyatan_screech",
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, "livyatan_screech"))
        );
        public static final Holder<SoundEvent> SHIP_HORN = SOUND_EVENTS.register(
                "ship_horn",
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, "shiphorn"))
        );
        public static final Holder<SoundEvent> LIVYATAN_FIGHT = SOUND_EVENTS.register(
                "livyatan_fight",
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID,"livyatan_fight"))
        );


public static final DeferredHolder<SoundEvent,SoundEvent> ECHOES_OF_THE_DEEP = registerJukeboxSong("echoes_of_the_deep");
public static final ResourceKey<JukeboxSong>  ECHOES_OF_THE_DEEP_KEY = createSong("echoes_of_the_deep");

        private static ResourceKey<JukeboxSong> createSong(String name) {
                return ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID,name));
        }

        private static DeferredHolder<SoundEvent, SoundEvent> registerJukeboxSong(String name) {
                return SOUND_EVENTS.register(name, ()->SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, name)));
        }

    }

