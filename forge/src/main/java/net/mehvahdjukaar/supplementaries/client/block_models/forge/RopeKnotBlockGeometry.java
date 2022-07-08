package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class RopeKnotBlockGeometry implements IUnbakedGeometry<RopeKnotBlockGeometry> {

    private final BlockModel knot;

    protected RopeKnotBlockGeometry(BlockModel overlay) {
        this.knot = overlay;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel bakedOverlay = this.knot.bake(bakery, knot, spriteGetter, modelTransform, modelLocation, true);
        return new RopeKnotBlockBakedModel(bakedOverlay);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return knot.getMaterials(modelGetter, missingTextureErrors);
    }
}