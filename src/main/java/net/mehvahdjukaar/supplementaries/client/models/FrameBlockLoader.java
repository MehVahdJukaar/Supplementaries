package net.mehvahdjukaar.supplementaries.client.models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class FrameBlockLoader  implements IModelLoader<FrameBlockGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public FrameBlockGeometry read(JsonDeserializationContext context, JsonObject json) {
        BlockModel model;
        model = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("overlay"));
        return new FrameBlockGeometry(model);
    }
}
