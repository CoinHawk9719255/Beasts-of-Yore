package com.themodone1.beastsofyore.datagen;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output)
    {
        super(output, BeastsofYore.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.CONCRETION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MYSTERIOUS_TOOTH.get(),  ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LIVYATAN_SPAWN_EGG.get(),  ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LIVYATAN_MEAT.get(),   ModelTemplates.FLAT_ITEM);






    }
}


