package com.themodone1.beastsofyore.entities;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.Livyatan;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities
{
    private static final int MAX_LIVYATANS_NEARBY = 1;
    private static final double CHECK_RADIUS = 64.0;

    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BeastsofYore.MOD_ID);


    public static final Supplier<EntityType<Livyatan>> LIVYATAN = ENTITIES.registerEntityType(
            "livyatan", Livyatan::new, MobCategory.WATER_CREATURE,
            //builder -> builder.sized(6.0f, 2.7f).clientTrackingRange(10)
            builder -> builder.sized(15.5f, 2.7f).clientTrackingRange(6)
    );
    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event){
        event.register(
                LIVYATAN.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                ((entityType, serverLevelAccessor, entitySpawnReason, blockPos, randomSource) -> {

                    if (!(serverLevelAccessor.getLevel() instanceof ServerLevel serverLevel)) {
                        return false;
                    }

                    AABB checkArea = AABB.ofSize(Vec3.atCenterOf(blockPos), 164.0, 64.0, 164.0);
                    if (serverLevel.getEntitiesOfClass(Livyatan.class, checkArea).size() >= 2) {
                        return false;
                    }

                    return Mob.checkMobSpawnRules(entityType, serverLevelAccessor, entitySpawnReason, blockPos, randomSource)
                            && serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER);
                }),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

}


