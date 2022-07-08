package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public record BlackboardBlockGeometry(
        BlockModel model) implements IUnbakedGeometry<BlackboardBlockGeometry> {


    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {

        var list = new ArrayList<Material>();
        list.add(this.model().getMaterial("white"));
        list.add(this.model().getMaterial("black"));
        list.addAll(model.getMaterials(modelGetter, missingTextureErrors));
        return list;
    }


    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel bakedOverlay = this.model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);
        return new BlackboardBakedModel(bakedOverlay, owner, bakery, spriteGetter, modelTransform, overrides);
    }

}