package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.gson.*;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.supplementaries.SuppClientPlatformStuff;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.Function;

public class AwningModelLoader implements CustomModelLoader {

    private static final BlockModel.Deserializer DESERIALIZER = new BlockModel.Deserializer();

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        if(!SuppClientPlatformStuff.hasFixedAO()){
            // vanilla AO code is so shit, full of bugs
            json.addProperty("ambientocclusion", false);
        }
        json.remove("loader");
        BlockModel modelHack = DESERIALIZER.deserialize(json, BlockModel.class, context);
        if (json.has("elements")) {
            JsonArray ja = GsonHelper.getAsJsonArray(json, "elements");
            for (int j = 0; j < ja.size() && j < modelHack.getElements().size(); ++j) {
                BlockElement element = modelHack.getElements().get(j);
                JsonElement elementJson = ja.get(j);
                if (elementJson instanceof JsonObject jo) {
                    JsonElement rot = jo.get("extra_rotation");
                    if (rot != null) {
                        element.rotation = new BlockElementRotation(
                                element.rotation.origin(), element.rotation.axis(),
                                element.rotation.angle() - rot.getAsFloat(), element.rotation.rescale()
                        );
                    }
                }
            }
        }

        return new CustomGeometry() {
            @Override
            public CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
                return null;
            }

            @Override
            public BakedModel bakeModel(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
                // resolve model and fix parent texture map as fore doesn't do that. if a child has parent of custom geometry, just THAT will be baked
                var parent = modelBaker.getModel(location);
                if (parent instanceof BlockModel bm) {
                    // super hacky
                    modelHack.textureMap.putAll(bm.textureMap);
                }
                return modelHack.bake(modelBaker, spriteGetter, transform, location);
            }
        };
    }

}
