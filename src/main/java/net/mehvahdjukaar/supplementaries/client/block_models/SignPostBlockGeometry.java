package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class SignPostBlockGeometry implements IModelGeometry<SignPostBlockGeometry> {


    protected SignPostBlockGeometry() {
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {

        return new SignPostBlockBakedModel();
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singletonList(new Material(TextureAtlas.LOCATION_BLOCKS, ClientRegistry.SIGN_POSTS_MATERIALS.get(WoodType.OAK_WOOD_TYPE).texture()));
    }
}