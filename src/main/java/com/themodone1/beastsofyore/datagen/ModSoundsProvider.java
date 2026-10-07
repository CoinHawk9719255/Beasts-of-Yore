package com.themodone1.beastsofyore.datagen;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.sounds.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundsProvider extends SoundDefinitionsProvider {

    public ModSoundsProvider(PackOutput output) {
        super(output, BeastsofYore.MOD_ID);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.LIVYATAN_DEATH.value(), definition()
                        .subtitle("sounds.beastsofyore.livyatan_death")
                        .with(sound(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID,
                                "fat_ahh_whale_die"))
                                .attenuationDistance(32)));
        add(ModSounds.SHIP_HORN.value(),definition()
                .subtitle("sounds.beastsofyore.ship_horn")
                .with(sound(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID,
                        "shiphorn"))
                        .attenuationDistance(32)));
        add(ModSounds.LIVYATAN_SCREECH.value(),definition()
                .subtitle("sounds.beastsofyore.livyatan_screech")
                .with(sound(Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID,
                        "whale_click"))
                        .attenuationDistance(32)));
    }
}
