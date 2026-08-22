package com.themodone1.beastsofyore.entities;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.Livyatan;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
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
    @SubscribeEvent
    public static void reggisterSpawnPlacements(RegisterSpawnPlacementsEvent event){
        event.register(
                LIVYATAN.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                ((Livyatan, serverLevelAccessor, entitySpawnReason, blockPos, randomSource) ->


                        Mob.checkMobSpawnRules(Livyatan,serverLevelAccessor,entitySpawnReason,blockPos,randomSource) && serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER)),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

}


