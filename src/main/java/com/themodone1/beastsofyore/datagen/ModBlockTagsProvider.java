package com.themodone1.beastsofyore.datagen;

import com.themodone1.beastsofyore.item.ModItems;
import com.themodone1.beastsofyore.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Blocks.NEEDS_LIVYATAN_TOOTH)
                    .add(Blocks.DIRT.defaultBlockState().getBlock())
                    .addTag(BlockTags.NEEDS_DIAMOND_TOOL);
            tag(ModTags.Blocks.INCORRECT_FOR_LIVYATAN_TOOTH_TOOL)
                    .addTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                    .remove(ModTags.Blocks.NEEDS_LIVYATAN_TOOTH);


    }
}
