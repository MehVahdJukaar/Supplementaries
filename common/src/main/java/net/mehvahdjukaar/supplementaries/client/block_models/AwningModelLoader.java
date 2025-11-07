package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.gson.*;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.supplementaries.SuppClientPlatformStuff;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.GsonHelper;

public class AwningModelLoader implements CustomModelLoader {

    private static final BlockModel.Deserializer DESERIALIZER = new BlockModel.Deserializer();

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        if (!SuppClientPlatformStuff.hasFixedAO()) {
            // vanilla AO code is so shit, full of bugs
            json.addProperty("ambientocclusion", false);
        }
        //prevents recursion
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

        return (modelBaker, spriteGetter, modelState) -> {
            // resolve model and fix parent texture map as forge doesn't do that. if a child has parent of custom geometry, just THAT will be baked
            if (modelState instanceof Variant bm) {
                // super hacky. Should have used IGeometryBakingContext from forge instead which has sstuff for sprites
                var parent = modelBaker.getModel(bm.getModelLocation());
                if (parent instanceof BlockModel m) {
                    modelHack.textureMap.putAll(m.textureMap);
                }
            }
            return modelHack.bake(modelBaker, spriteGetter, modelState);
        };
    }

}
