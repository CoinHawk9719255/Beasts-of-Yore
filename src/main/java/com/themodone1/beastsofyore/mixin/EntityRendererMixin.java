package com.themodone1.beastsofyore.mixin;

import com.themodone1.beastsofyore.entities.LivyatanCore.Livyatan;
import com.themodone1.beastsofyore.entities.LivyatanCore.LivyatanPart;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void beastsofyore$cullWithParts(Entity entity, Frustum culler, double camX, double camY, double camZ,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && entity instanceof Livyatan whale
                && whale.shouldRender(camX, camY, camZ)) {
            for (LivyatanPart part : whale.getSubEntities()) {
                if (culler.isVisible(part.getBoundingBox().inflate(2.0))) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}