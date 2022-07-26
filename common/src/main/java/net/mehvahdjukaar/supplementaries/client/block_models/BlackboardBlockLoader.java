package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class BlackboardBlockLoader implements CustomModelLoader {

    @Override
    public Geometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        BlockModel model = ClientPlatformHelper.parseBlockModel(json.get("model"));
        return new Geometry(model);
    }

    private record Geometry(BlockModel model) implements CustomGeometry {


        @Override
        public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            var list = new ArrayList<>(model.getMaterials(modelGetter, missingTextureErrors));
            list.add(this.model().getMaterial("white"));
            list.add(this.model().getMaterial("black"));
            return list;
        }


        @Override
        public CustomBakedModel bake(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation) {
            BakedModel bakedOverlay = this.model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);
            return new BlackboardBakedModel(model,bakedOverlay, spriteGetter, modelTransform);
        }

    }

}
