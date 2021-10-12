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

public class WallLanternGeometry implements IModelGeometry<WallLanternGeometry> {

    private final BlockModel support;

    protected WallLanternGeometry(BlockModel support) {
        this.support = support;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel bakedOverlay = this.support.bake(bakery, support, spriteGetter, modelTransform, modelLocation, true);
        return new WallLanternBakedModel(bakedOverlay);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return support.getMaterials(modelGetter, missingTextureErrors);
    }
}