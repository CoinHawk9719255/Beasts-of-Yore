package com.themodone1.beastsofyore.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.entities.LivyatanDoom.ThrownLivyatanDoom;
import com.themodone1.beastsofyore.renders.models.LivyatanDoomModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;


public class ThrownLivyatanDoomRenderer extends EntityRenderer<ThrownLivyatanDoom,ThrownLivyatanDoomRenderState> {
    public static final Identifier LIVYATAN_DOOM_LOCATION = Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID,"textures/entity/livyatan_doom.png");
    private final LivyatanDoomModel model;

    public ThrownLivyatanDoomRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LivyatanDoomModel(context.bakeLayer(LivyatanDoomModel.LAYER));
    }
    public void submit(ThrownLivyatanDoomRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 90.0F));
        submitNodeCollector.order(0).submitModel(this.model, Unit.INSTANCE, poseStack, LIVYATAN_DOOM_LOCATION, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
        if (state.isFoil) {
            submitNodeCollector.order(1).submitModel(this.model, Unit.INSTANCE, poseStack, ItemFeatureRenderer.getFoilRenderType(this.model.renderType(LIVYATAN_DOOM_LOCATION), false), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
        }

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
    public ThrownLivyatanDoomRenderState createRenderState() {
        return new ThrownLivyatanDoomRenderState();
    }
    public void extractRenderState(ThrownLivyatanDoom entity, ThrownLivyatanDoomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.isFoil = entity.isFoil();
    }
}
