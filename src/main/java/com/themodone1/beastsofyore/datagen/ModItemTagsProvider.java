package com.themodone1.beastsofyore.datagen;

import com.themodone1.beastsofyore.item.ModItems;
import com.themodone1.beastsofyore.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.LIVYATAN_TOOTH_REPAIRABLE)
                .add(ModItems.LIVYATAN_TOOTH.get());
        tag(ModTags.Items.DISC_LIVYATAN)
                .add(ModItems.LIVYATAN_DOOM.get());
        tag(ItemTags.SWORDS).add(ModItems.LIVYATAN_DOOM.get());

    }
}
