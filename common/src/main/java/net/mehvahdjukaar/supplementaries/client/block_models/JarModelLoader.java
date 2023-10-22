package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.minecraft.util.GsonHelper;

import static net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader.parseModel;

public class JarModelLoader implements CustomModelLoader {

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var model = json.get("model");
        float width = GsonHelper.getAsFloat(json, "liquid_width") / 16f;
        float height = GsonHelper.getAsFloat(json, "liquid_max_height") / 16f;
        float y0 = GsonHelper.getAsFloat(json, "liquid_y_offset") / 16f;
        return (modelBaker, spriteGetter, transform, location) -> {
            var g = parseModel(model, modelBaker, spriteGetter, transform, location);
            return new JarBakedModel(g, width, height, y0, transform);
        };
    }

}
