package com.themodone1.beastsofyore.datagen;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Beastsofyore Recipes";
        }
    }

    @Override
    protected void buildRecipes() {

        shaped(RecipeCategory.COMBAT,ModItems.LIVYATAN_DOOM.get())
                .pattern("  T")
                .pattern(" S ")
                .pattern("S  ")
                .define('S', Ingredient.of(Items.STICK))
                        .define('T', Ingredient.of(ModItems.LIVYATAN_TOOTH))
                                .group("Beastsofyore Recipes")
                .unlockedBy("has_livyatan_tooth",has(ModItems.LIVYATAN_TOOTH))
                                        .save(output);


        SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(ModItems.LIVYATAN_MEAT),
                RecipeCategory.FOOD,
                CookingBookCategory.FOOD,
                ModItems.COOKED_LIVYATAN_MEAT,
                3.0f,
                300

        ).unlockedBy("has_livyatan_meat", has(ModItems.LIVYATAN_MEAT))
                .save(this.output,BeastsofYore.MOD_ID + ":livyatan_cooking");
    }
}




