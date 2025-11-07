package net.mehvahdjukaar.supplementaries.integration;


public class FlywheelCompat {

    //TODO: add more instances
    public static void setupClient() {
        /*
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
                */

        //    InstancedRenderRegistry.configure(ModRegistry.WIND_VANE_TILE.get()).alwaysSkipRender().factory(WindVaneInstance::new).apply();
        //    InstancedRenderRegistry.configure(ModRegistry.CANNON_TILE.get()).factory(CannonInstance::new).apply();
    }

}
