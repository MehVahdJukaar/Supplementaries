package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class HangingSignLoader implements IModelLoader<HangingSignGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public HangingSignGeometry read(JsonDeserializationContext context, JsonObject json) {
        BlockModel stick = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("stick"));
        BlockModel leftFence = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("left_post"));
        BlockModel leftPalisade = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("left_palisade"));
        BlockModel leftWall = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("left_wall"));
        BlockModel leftBeam = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("left_beam"));
        BlockModel rightFence = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("right_post"));
        BlockModel rightPalisade = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("right_palisade"));
        BlockModel rightWall = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("right_wall"));
        BlockModel rightBeam = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("right_beam"));
        BlockModel leftStick = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("left_stick"));
        BlockModel rightStick = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("right_stick"));
        return new HangingSignGeometry(stick, leftFence, leftPalisade, leftWall, leftBeam, leftStick,
                rightFence, rightPalisade, rightWall, rightBeam, rightStick);
    }
}
