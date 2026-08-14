package com.themodone1.beastsofyore.entities;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.Livyatan;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities
{
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BeastsofYore.MOD_ID);


    public static final Supplier<EntityType<Livyatan>> LIVYATAN = ENTITIES.registerEntityType(
            "livyatan", Livyatan::new, MobCategory.WATER_CREATURE,
            //builder -> builder.sized(6.0f, 2.7f).clientTrackingRange(10)
            builder -> builder.sized(15.5f, 2.7f).clientTrackingRange(6)
    );


}


