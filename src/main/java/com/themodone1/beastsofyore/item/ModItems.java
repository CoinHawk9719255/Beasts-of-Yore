package com.themodone1.beastsofyore.item;


import com.themodone1.beastsofyore.BeastsofYore;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BeastsofYore.MOD_ID);

    public static final DeferredItem<Item> CONCRETION = ITEMS.registerSimpleItem("concretion");
    public static final DeferredItem<Item> MYSTERIOUS_TOOTH = ITEMS.registerSimpleItem("mysterious_tooth");




    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
