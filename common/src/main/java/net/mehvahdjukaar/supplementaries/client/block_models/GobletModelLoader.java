package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;

import static net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader.parseModel;

public class GobletModelLoader implements CustomModelLoader {

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var goblet = json.get("model");
        var liquid = json.get("liquid");
        return (modelBaker, spriteGetter, transform, location) -> {

            var g = parseModel(goblet, modelBaker, spriteGetter, transform, location);
            var l = parseModel(liquid, modelBaker, spriteGetter, transform, location);
            return new GobletBakedModel(g, l, transform);
        };
    }


}
