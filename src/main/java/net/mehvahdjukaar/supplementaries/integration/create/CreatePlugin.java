package net.mehvahdjukaar.supplementaries.integration.create;


import com.simibubi.create.AllMovementBehaviours;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.create.behaviors.BambooSpikesBehavior;
import net.mehvahdjukaar.supplementaries.integration.create.behaviors.HourglassBehavior;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;

public class CreatePlugin {
    public static void initialize() {

        try {
            AllMovementBehaviours.registerBehaviour(ModRegistry.BAMBOO_SPIKES.get().delegate, new BambooSpikesBehavior());
            AllMovementBehaviours.registerBehaviour(ModRegistry.HOURGLASS.get().delegate, new HourglassBehavior());
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: " + e);
        }

    }


}
