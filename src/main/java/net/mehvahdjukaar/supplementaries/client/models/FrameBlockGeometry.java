package net.mehvahdjukaar.supplementaries.client.models;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class FrameBlockGeometry implements IModelGeometry<FrameBlockGeometry> {

    private final BlockModel overlay;
    protected FrameBlockGeometry(BlockModel overlay) {
        this.overlay = overlay;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel bakedOverlay = this.overlay.bake(bakery,overlay,spriteGetter,modelTransform,modelLocation,true);
        return new FrameBlockBakedModel(bakedOverlay);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return overlay.getMaterials(modelGetter,missingTextureErrors);
    }
}