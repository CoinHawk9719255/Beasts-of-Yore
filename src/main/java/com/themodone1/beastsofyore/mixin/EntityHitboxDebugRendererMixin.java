package com.themodone1.beastsofyore.mixin;

import com.themodone1.beastsofyore.entities.LivyatanCore.Livyatan;
import com.themodone1.beastsofyore.entities.LivyatanCore.LivyatanPart;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {

    @Inject(method = "showHitboxes", at = @At("TAIL"))
    private void beastsofyore$drawLivyatanParts(Entity entity, float partialTicks, boolean isServerEntity, CallbackInfo ci) {
        if (entity instanceof Livyatan livyatan) {
            for (LivyatanPart part : livyatan.getSubEntities()) {
                Vec3 offset = part.getPosition(partialTicks).subtract(part.position());
                Gizmos.cuboid(
                        part.getBoundingBox().move(offset),
                        GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 0.25F, 1.0F, 0.0F))
                );
            }
        }
    }
}