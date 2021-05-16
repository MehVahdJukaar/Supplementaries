package net.mehvahdjukaar.supplementaries.client.renderers;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class FrameBlockLoader  implements IModelLoader<FrameBlockGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public FrameBlockGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new FrameBlockGeometry();
    }
}
