package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BookPileModelLoader implements CustomModelLoader {

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {

        return (modelBaker, spriteGetter, transform) -> {
            Map<ResourceLocation, BakedModel> bookIdToModel = new HashMap<>();
            JsonObject books = json.getAsJsonObject("books");
            for (var entry : books.entrySet()) {
                ResourceLocation id = ResourceLocation.parse(entry.getKey());
                JsonElement element = entry.getValue();
                BakedModel m = CustomModelLoader.parseModel(element, modelBaker, spriteGetter, transform);
                bookIdToModel.put(id, m);
            }
            return new BookPileModel(bookIdToModel, transform);
        };
    }


}