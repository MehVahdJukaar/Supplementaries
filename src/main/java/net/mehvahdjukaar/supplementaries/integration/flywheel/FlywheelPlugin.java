package net.mehvahdjukaar.supplementaries.integration.flywheel;


import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import net.mehvahdjukaar.supplementaries.integration.flywheel.instances.BellowsInstance;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;

public class FlywheelPlugin {

    //TODO: add more tiles
    public static void registerInstances() {
        InstancedRenderRegistry.configure(ModRegistry.BELLOWS_TILE.get()).alwaysSkipRender().factory(BellowsInstance::new).apply();
    }

}
