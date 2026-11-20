package com.themodone1.beastsofyore.entities;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.entities.LivyatanCore.Livyatan;
import com.themodone1.beastsofyore.entities.LivyatanDoom.ThrownLivyatanDoom;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModEntities{


    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BeastsofYore.MOD_ID);

    public static final Supplier<EntityType<Livyatan>> LIVYATAN = ENTITIES.registerEntityType(
            "livyatan", Livyatan::new, MobCategory.WATER_CREATURE,
            builder -> builder.sized(3.2f, 2.7f).clientTrackingRange(6)
    );
    public static final Supplier<EntityType<ThrownLivyatanDoom>>
    THROWN_LIVYATAN_DOOM = ENTITIES.registerEntityType(
            "thrown_livyatan_doom", ThrownLivyatanDoom::new, MobCategory.MISC,
            builder -> builder.sized(
                    0.5f, 0.5f)

                    .clientTrackingRange(4)
                    .eyeHeight(0.13F)
                    .updateInterval(10)
                    .canSpawnFarFromPlayer()
                    .immuneTo(Blocks.WATER)

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

                    return Mob.checkMobSpawnRules(entityType, serverLevelAccessor, entitySpawnReason, blockPos, randomSource)
                            && serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER);
                }),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
}


