package net.mehvahdjukaar.supplementaries.client.block_models.forge;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class BlackboardBlockLoader implements IGeometryLoader<BlackboardBlockGeometry> {

    @Override
    public BlackboardBlockGeometry read(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        BlockModel model = ExtendedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("model"));
        //SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);
        return new BlackboardBlockGeometry(model);
    }

}
