package net.mehvahdjukaar.supplementaries.integration;


import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BellowsBlockTile;
import net.mehvahdjukaar.supplementaries.integration.flywheel.BellowsInstance;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;

public class FlywheelCompat {

    //TODO: add more instances
    public static void setupClient() {
        VisualizerRegistry.setVisualizer(ModRegistry.BELLOWS_TILE.get(),
                new BlockEntityVisualizer<>() {
                    @Override
                    public BlockEntityVisual<? super BellowsBlockTile> createVisual(VisualizationContext visualizationContext, BellowsBlockTile bellowsBlockTile, float v) {
                        return new BellowsInstance(visualizationContext, bellowsBlockTile, v);
                    }

                    @Override
                    public boolean skipVanillaRender(BellowsBlockTile bellowsBlockTile) {
                        return true;
                    }
                });
        //    InstancedRenderRegistry.configure(ModRegistry.WIND_VANE_TILE.get()).alwaysSkipRender().factory(WindVaneInstance::new).apply();
        //    InstancedRenderRegistry.configure(ModRegistry.CANNON_TILE.get()).factory(CannonInstance::new).apply();
    }

}
