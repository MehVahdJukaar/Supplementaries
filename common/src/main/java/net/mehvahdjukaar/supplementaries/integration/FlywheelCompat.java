package net.mehvahdjukaar.supplementaries.integration;


import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.backend.Backend;

public class FlywheelCompat {

    //TODO: add more instances
    public static void setupClient() {
      //  InstancedRenderRegistry.configure(ModRegistry.BELLOWS_TILE.get()).alwaysSkipRender().factory(BellowsInstance::new).apply();
    //    InstancedRenderRegistry.configure(ModRegistry.WIND_VANE_TILE.get()).alwaysSkipRender().factory(WindVaneInstance::new).apply();
    //    InstancedRenderRegistry.configure(ModRegistry.CANNON_TILE.get()).factory(CannonInstance::new).apply();
    }


    public static boolean isActive() {
        return false;
    }
}
