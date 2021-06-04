package net.mehvahdjukaar.supplementaries.client.models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class BlackboardBlockLoader implements IModelLoader<BlackboardBlockGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public BlackboardBlockGeometry read(JsonDeserializationContext context, JsonObject json) {

        BlockModel model = null;
        model = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("model"));
        //SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);
        String retextured = JSONUtils.getAsString(json, "retexture");
        return new BlackboardBlockGeometry(model, retextured);
    }

}
