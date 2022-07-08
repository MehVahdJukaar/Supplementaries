package net.mehvahdjukaar.supplementaries.client.block_models.forge;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class RopeKnotBlockLoader implements IGeometryLoader<RopeKnotBlockGeometry> {

    @Override
    public RopeKnotBlockGeometry read(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        BlockModel model;
        model = ExtendedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("knot"));
        return new RopeKnotBlockGeometry(model);
    }
}