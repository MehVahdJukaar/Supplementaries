package net.mehvahdjukaar.supplementaries.client.models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class MimicBlockLoader implements IModelLoader<MimicBlockGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public MimicBlockGeometry read(JsonDeserializationContext context, JsonObject json) {
        return new MimicBlockGeometry();
    }
}
