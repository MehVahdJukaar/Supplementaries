package net.mehvahdjukaar.supplementaries.client.models;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class BlackboardBlockGeometry implements IModelGeometry<BlackboardBlockGeometry> {

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

    private final BlockModel model;
    private final String toRetextureName;


    protected BlackboardBlockGeometry(BlockModel model, String toRetextureName) {
        this.model = model;
        this.toRetextureName = toRetextureName;
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
        Collection<Material> mat = model.getMaterials(modelGetter,missingTextureErrors);
return mat;
        //mat.add(new RenderMaterial(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS,mat)));
    }



    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        // fetch textures before rebaking

        return new BlackboardBakedModel(this.model,owner,bakery,spriteGetter,modelTransform,overrides,modelLocation,this.toRetextureName);
    /*

        // making two models, normal and frosted
        List<BlockPart> normalElements = new ArrayList<>();
        List<BlockPart> frontElements = new ArrayList<>();
        for (BlockPart part : model.getElements()) {
            Map<Direction, BlockPartFace> normalFaces = new EnumMap<>(Direction.class);
            Map<Direction, BlockPartFace> frontFaces = new EnumMap<>(Direction.class);
            for (Entry<Direction,BlockPartFace> entry : part.faces.entrySet()) {
                BlockPartFace face = entry.getValue();
                // if the texture is liquid, update the tint index and insert into the liquid list
                if (texturesToReplace.contains(face.texture.substring(1))) {
                    frontFaces.put(entry.getKey(), new BlockPartFace(face.cullForDirection, -1, face.texture, face.uv));
                } else {
                    // otherwise use original face and make a copy for frost
                    normalFaces.put(entry.getKey(), face);
                }
            }
            // if we had a liquid face, make a new part for the warm elements and add a liquid element
            BlockPart newPart = part;
            if (!frontFaces.isEmpty()) {
                newPart = new BlockPart(part.from, part.to, normalFaces, part.rotation, part.shade);

                frontElements.add(new BlockPart(part.from, part.to, frontFaces, part.rotation, part.shade));
            }
            // frosted has all elements of normal, plus an overlay when relevant
            normalElements.add(newPart);
        }

        // make a list of parts with warm and liquid for the base model
        List<BlockPart> firstBake = new ArrayList<>(normalElements);
        firstBake.addAll(frontElements);



        // if nothing retextured, bake frosted and return simple baked model
        //return bakeModel(owner, firstBake, modelTransform, overrides, spriteGetter, modelLocation);

        // if nothing retextured, bake frosted and return simple baked model
        //IBakedModel baked = SimpleBlockModel.bakeModel(owner, firstBake, modelTransform, overrides, spriteGetter, modelLocation);
        //return  model.bake(bakery, model, spriteGetter, modelTransform, modelLocation,  true);
        // full dynamic baked model
       // return new BlackboardBakedModel(baked, owner, model, modelTransform, texturesToReplace);

        return SimpleBlockModel.bakeDynamic(new RetexturedModelConfiguration(owner, texturesToReplace, Textures.MAGMA_TEXTURE),
                model.getElements(), modelTransform, overrides, spriteGetter, modelLocation);
        //return SimpleBlockModel.bakeDynamic(new BlackboardBakedModel.RetexturedConfiguration(owner, texturesToReplace, Textures.MAGMA_TEXTURE), model.getElements(), modelTransform);
        */
    }


}