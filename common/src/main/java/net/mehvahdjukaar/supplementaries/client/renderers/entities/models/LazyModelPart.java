package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

import java.util.function.Supplier;

public class LazyModelPart implements Supplier<ModelPart> {

    private final ModelLayerLocation location;
    private final String partName;
    private ModelPart instance;

    public LazyModelPart(ModelLayerLocation location, String partName) {
        this.location = location;
        this.partName = partName;
    }

    @Override
    public ModelPart get() {
        if (instance == null) {
            instance = Minecraft.getInstance().getEntityModels().bakeLayer(location).getChild(partName);
        }
        return instance;
    }

    public static LazyModelPart of(ModelLayerLocation location, String partName) {
        return new LazyModelPart(location, partName);
    }
}
