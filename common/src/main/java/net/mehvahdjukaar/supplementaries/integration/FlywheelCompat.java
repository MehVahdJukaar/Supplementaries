package net.mehvahdjukaar.supplementaries.integration;


import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import net.mehvahdjukaar.supplementaries.integration.flywheel.BellowsInstance;
import net.mehvahdjukaar.supplementaries.integration.flywheel.CannonInstance;
import net.mehvahdjukaar.supplementaries.integration.flywheel.WindVaneInstance;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;

public class FlywheelCompat {

    //TODO: add more instances
    public static void setupClient() {
        InstancedRenderRegistry.configure(ModRegistry.BELLOWS_TILE.get()).alwaysSkipRender().factory(BellowsInstance::new).apply();
        InstancedRenderRegistry.configure(ModRegistry.WIND_VANE_TILE.get()).alwaysSkipRender().factory(WindVaneInstance::new).apply();
        InstancedRenderRegistry.configure(ModRegistry.CANNON_TILE.get()).factory(CannonInstance::new).apply();
    }


    public static boolean isOn() {
        return Backend.isOn();
    }
}
