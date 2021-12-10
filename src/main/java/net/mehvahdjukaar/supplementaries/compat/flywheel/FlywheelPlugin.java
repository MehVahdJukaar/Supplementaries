package net.mehvahdjukaar.supplementaries.compat.flywheel;

import com.jozufozu.flywheel.backend.OptifineHandler;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import net.mehvahdjukaar.supplementaries.compat.flywheel.instances.BellowsInstance;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;

public class FlywheelPlugin {

    //TODO: add more tiles
    public static void registerInstances() {
        InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();
        r.tile(ModRegistry.BELLOWS_TILE.get()).setSkipRender(true).factory(BellowsInstance::new);
    }

    public static boolean areShadersOn(){
        return OptifineHandler.usingShaders();
    }
}
