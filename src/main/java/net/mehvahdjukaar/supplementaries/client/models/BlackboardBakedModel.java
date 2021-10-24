package net.mehvahdjukaar.supplementaries.client.models;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;

public class BlackboardBakedModel implements IDynamicBakedModel {
    //model cache used when switching frequently between models
    // data needed to rebake

    private final IModelConfiguration owner;
    private final ModelBakery bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final ItemOverrides overrides;
    private final ResourceLocation modelLocation;

    private final BlockModel unbaked;
    private final String toRetextureName;

    public BlackboardBakedModel(BlockModel unbaked, IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation, String toRetextureName) {
        this.unbaked = unbaked;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
        this.modelLocation = modelLocation;
        this.toRetextureName = toRetextureName;
    }

    private BakedModel rebake(ResourceLocation replacement) {

        return null;

        // if we have liquid elements, add them
        //List<BlockPart> elements = new ArrayList<>(baseElements);
        // if no offset, copy in liquid list exactly

        //elements.addAll(this.liquidElements);


        // bake the new model
        //return SimpleBlockModel.bakeDynamic(new RetexturedConfiguration(owner, retextured, replacementTexture), elements, transform);
    }


    @Override
    public boolean useAmbientOcclusion() {
        return unbaked.hasAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return spriteGetter.apply(owner.resolveTexture("particle"));
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }



/*

    public static IBakedModel bakeModel(IModelConfiguration owner, List<BlockPart> elements, IModelTransform transform, ItemOverrideList overrides, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, ResourceLocation location) {
        TextureAtlasSprite particle = (TextureAtlasSprite)spriteGetter.apply(owner.resolveTexture("particle"));
        SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(owner, overrides)).particle(particle);

        for (BlockPart part : elements) {

            for (Direction direction : part.faces.keySet()) {
                BlockPartFace face = part.faces.get(direction);
                String texture = face.texture;
                if (texture.charAt(0) == '#') {
                    texture = texture.substring(1);
                }

                TextureAtlasSprite sprite = spriteGetter.apply(owner.resolveTexture(texture));
                if (face.cullForDirection == null) {
                    builder.addUnculledFace(BlockModel.bakeFace(part, face, sprite, direction, transform, location));
                } else {
                    builder.addCulledFace(Direction.rotate(transform.getRotation().getMatrix(), face.cullForDirection), BlockModel.bakeFace(part, face, sprite, direction, transform, location));
                }
            }
        }

        return builder.build();
    }
    */

    public static Set<String> getAllRetextured(IModelConfiguration owner, BlockModel model, String originalSet) {

        Set<String> retextured = Sets.newHashSet(originalSet);

        model.textureMap.forEach((name, either) -> either.ifRight((parent) -> {
            if (retextured.contains(parent)) retextured.add(name);
        }));

        return ImmutableSet.copyOf(retextured);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random, IModelData data) {


        if (data != EmptyModelData.INSTANCE) {
            BlackboardKey key = null;//data.getData(BlackboardBlockTile.TEXTURE);
            if(key!=null) {
                //Set<String> retextured = getAllRetextured(owner,unbaked,toRetextureName);
                //ResourceLocation texture = BlackboardTextureManager.INSTANCE.getResoucelocation(key);
                //IBakedModel baked2 = SimpleBlockModel.bakeModel(new RetexturedModelConfiguration(owner, retextured, texture),
                //        unbaked.getElements(), modelTransform,overrides,spriteGetter,modelLocation);
                //return baked2.getQuads(state,direction,random,data);
                ResourceLocation texture = BlackboardTextureManager.INSTANCE.getResourceLocation(key);
                this.unbaked.textureMap.replace(this.toRetextureName, Either.right(texture.toString()));
            }
        }
        BakedModel baked = this.unbaked.bake(bakery,unbaked,spriteGetter,modelTransform,modelLocation,true);
        return baked.getQuads(state,direction,random,data);
        /*
        ResourceLocation texture;
        if (data == EmptyModelData.INSTANCE) {
            texture = MissingTextureSprite.getLocation();
        }
        else {
            // get texture name, if missing use missing
            // also use missing if no retextured, that just makes the cache smaller for empty cauldron

            Black = data.getData(BlackboardBlockTile.TEXTURE);
            if (texture == null) {
                texture = MissingTextureSprite.getLocation();
            } else {
                // serverside uses texture "name" rather than path, use the sprite getter to translate
                texture = Textures.HONEY_TEXTURE;
            }
        }

        // fetch liquid offset amount

        // determine model variant
        IBakedModel baked = null;//warmBakery.apply(texture);
        // return quads



        return baked.getQuads(state, direction, random, data);
        */

    }
}


