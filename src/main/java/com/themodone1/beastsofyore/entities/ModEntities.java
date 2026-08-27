package com.themodone1.beastsofyore.entities;

import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.Livyatan;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import java.util.function.Supplier;

//@EventBusSubscriber(modid = BeastsofYore.MOD_ID)
public class ModEntities
{
    private static final int MAX_LIVYATANS_NEARBY = 1;
    private static final double CHECK_RADIUS = 64.0;
    private static final int MAX_LIVYATANS_TOTAL = 3;

    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BeastsofYore.MOD_ID);

    public static final Supplier<EntityType<Livyatan>> LIVYATAN = ENTITIES.registerEntityType(
            "livyatan", Livyatan::new, MobCategory.WATER_CREATURE,
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

                    AABB checkArea = AABB.ofSize(Vec3.atCenterOf(blockPos), 64.0, 64.0, 64.0);
                    if (serverLevel.getEntitiesOfClass(Livyatan.class, checkArea).size() >= 2) {
                        System.out.println("yo theres too many here");
                        return false;
                    }

                    return Mob.checkMobSpawnRules(entityType, serverLevelAccessor, entitySpawnReason, blockPos, randomSource)
                            && serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER);
                }),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Livyatan)) return;
        if (event.getSpawnType() != EntitySpawnReason.NATURAL) return;
        if (!(event.getLevel().getLevel() instanceof ServerLevel serverLevel)) return;

        long count = serverLevel.getEntities(EntityTypeTest.forClass(Livyatan.class), AABB.INFINITE, entity ->  true).size();

        if (count >= MAX_LIVYATANS_TOTAL) {
            event.setSpawnCancelled(true);
        }
    }
}


