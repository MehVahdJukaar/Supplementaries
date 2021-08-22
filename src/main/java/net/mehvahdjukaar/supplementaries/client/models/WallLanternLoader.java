package net.mehvahdjukaar.supplementaries.client.models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class WallLanternLoader implements IModelLoader<WallLanternGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public WallLanternGeometry read(JsonDeserializationContext context, JsonObject json) {
        BlockModel model;
        model = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("support"));
        return new WallLanternGeometry(model);
    }
}
