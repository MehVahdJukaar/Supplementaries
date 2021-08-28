package net.mehvahdjukaar.supplementaries.compat.flywheel;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import net.mehvahdjukaar.supplementaries.compat.flywheel.instances.BellowsInstance;
import net.mehvahdjukaar.supplementaries.setup.Registry;

public class FlywheelPlugin {

    //TODO: add more tiles
    public static void registerInstances() {
        InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();
        r.tile(Registry.BELLOWS_TILE.get()).setSkipRender(true).factory(BellowsInstance::new);
    }
}
