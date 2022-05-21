package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public record BlackboardBlockGeometry(
        BlockModel model) implements IModelGeometry<BlackboardBlockGeometry> {

    /*
    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new FrameBlockBakedModel();
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singletonList(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, Textures.BROWN_CONCRETE_TEXTURE));
    }
    */

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {

       var list = new ArrayList<Material>();
       list.add(this.model().getMaterial("white"));
       list.add(this.model().getMaterial("black"));
       list.addAll(model.getMaterials(modelGetter, missingTextureErrors));
        return list;
        //mat.add(new RenderMaterial(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS,mat)));
    }


    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        // fetch textures before rebaking
        BakedModel bakedOverlay = this.model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);

        return new BlackboardBakedModel(bakedOverlay, owner, bakery, spriteGetter, modelTransform, overrides);
    }


}