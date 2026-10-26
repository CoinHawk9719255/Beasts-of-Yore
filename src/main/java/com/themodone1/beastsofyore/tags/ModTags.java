package com.themodone1.beastsofyore.tags;

import com.themodone1.beastsofyore.BeastsofYore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_LIVYATAN_TOOTH = createTag("needs_livyatan_tooth");
        public static final TagKey<Block> INCORRECT_FOR_LIVYATAN_TOOTH_TOOL = createTag("incorrect_for_livyatan_tooth");

        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> LIVYATAN_TOOTH_REPAIRABLE = createTag("livyatan_tooth_repairable");
        public static final TagKey<Item> DISC_LIVYATAN = createTag("disc_livyatan");

        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, name));
        }
    }
}