package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class BlackboardBlockLoader implements IModelLoader<BlackboardBlockGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public BlackboardBlockGeometry read(JsonDeserializationContext context, JsonObject json) {

        BlockModel model = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("model"));
        //SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);
        return new BlackboardBlockGeometry(model);
    }

}
