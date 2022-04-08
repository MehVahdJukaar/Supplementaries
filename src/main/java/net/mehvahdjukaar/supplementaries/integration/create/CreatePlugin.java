package net.mehvahdjukaar.supplementaries.integration.create;


import com.simibubi.create.AllMovementBehaviours;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.create.behaviors.BambooSpikesBehavior;
import net.mehvahdjukaar.supplementaries.integration.create.behaviors.HourglassBehavior;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;

public class CreatePlugin {
    public static void initialize() {

        try {
            AllMovementBehaviours.addMovementBehaviour(ModRegistry.BAMBOO_SPIKES.get(), new BambooSpikesBehavior());
            AllMovementBehaviours.addMovementBehaviour(ModRegistry.HOURGLASS.get(), new HourglassBehavior());
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: " + e);
        }

    }


}
