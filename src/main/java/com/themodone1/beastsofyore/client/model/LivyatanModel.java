package com.themodone1.beastsofyore.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.themodone1.beastsofyore.BeastsofYore;
import com.themodone1.beastsofyore.LivyatanCore.Livyatan;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class LivyatanModel extends GeoModel<Livyatan>
{
    private final Identifier model_path = Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, "livyatan");
    private final Identifier texture_path = Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, "textures/entity/livyatan.png");
    private final Identifier animation_path = Identifier.fromNamespaceAndPath(BeastsofYore.MOD_ID, "livyatan");
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return this.model_path;
    }


    @Override
    public @NonNull Identifier getAnimationResource(Livyatan animatable) {
        return this.animation_path;
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return this.texture_path;
    }

}
