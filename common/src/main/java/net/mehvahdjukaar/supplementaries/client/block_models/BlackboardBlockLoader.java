package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class BlackboardBlockLoader implements CustomModelLoader {

    @Override
    public Geometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new Geometry(ResourceLocation.tryParse(GsonHelper.getAsString(json, "frame")));
    }

    private record Geometry(ResourceLocation modelRes) implements CustomGeometry {

        @Override
        public CustomBakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation) {
            UnbakedModel model = bakery.getModel(modelRes);
            BakedModel bakedOverlay = model.bake(bakery, spriteGetter, modelTransform, modelLocation);
            return new BlackboardBakedModel((BlockModel) model,bakedOverlay, spriteGetter, modelTransform);
        }

    }

}
