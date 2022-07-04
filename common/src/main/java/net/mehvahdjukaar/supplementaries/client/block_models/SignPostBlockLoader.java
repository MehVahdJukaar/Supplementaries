package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class SignPostBlockLoader implements IModelLoader<SignPostBlockGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public SignPostBlockGeometry read(JsonDeserializationContext context, JsonObject json) {
        return new SignPostBlockGeometry();
    }
}
