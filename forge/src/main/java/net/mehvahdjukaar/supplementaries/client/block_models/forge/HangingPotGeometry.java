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

public class HangingPotGeometry implements IUnbakedGeometry<HangingPotGeometry> {

    private final BlockModel rope;

    protected HangingPotGeometry(BlockModel rope) {
        this.rope = rope;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel bakedOverlay = this.rope.bake(bakery, rope, spriteGetter, modelTransform, modelLocation, true);
        return new HangingPotBakedModel(bakedOverlay);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return rope.getMaterials(modelGetter, missingTextureErrors);
    }
}