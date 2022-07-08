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

public class FrameBlockGeometry implements IUnbakedGeometry<FrameBlockGeometry> {

    private final BlockModel overlay;

    protected FrameBlockGeometry(BlockModel overlay) {
        this.overlay = overlay;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel bakedOverlay = this.overlay.bake(bakery, overlay, spriteGetter, modelTransform, modelLocation, true);
        return new FrameBlockBakedModel(bakedOverlay);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return overlay.getMaterials(modelGetter, missingTextureErrors);
    }
}