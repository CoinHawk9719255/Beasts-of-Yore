package com.themodone1.beastsofyore.CreativeModeTab;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BeastsofYore.MOD_ID);

    public static final Supplier<CreativeModeTab> BEASTS_OF_YORE_TAB = CREATIVE_MODE_TABS.register(
            "beasts_of_yore_tab", () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.MYSTERIOUS_TOOTH.get()))
                    .title(Component.translatable("creativemodetab.beastsofyore.tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.MYSTERIOUS_TOOTH);
                        output.accept(ModItems.ARGENTAVIS_EGG);
                        output.accept(ModItems.CONCRETION);
                        output.accept(ModItems.LIVYATAN_MEAT);
                        output.accept(ModItems.LIVYATAN_SPAWN_EGG);
                        output.accept(ModItems.LIVYATAN_TOOTH);
                        output.accept(ModItems.ECHOES_OF_THE_DEEP_MUSIC_DISC);
                        output.accept(ModItems.ARGENTAVIS_FEATHER);
                        output.accept(ModItems.COOKED_LIVYATAN_MEAT);
                        output.accept(ModItems.LIVYATAN_DOOM);






                    })
                    .build());




    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
