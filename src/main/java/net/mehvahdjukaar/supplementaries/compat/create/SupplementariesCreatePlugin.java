package net.mehvahdjukaar.supplementaries.compat.create;

import com.simibubi.create.AllMovementBehaviours;


import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.curiosities.bell.AbstractBellBlock;
import com.simibubi.create.content.curiosities.bell.HauntedBellBlock;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.compat.create.behaviors.BambooSpikesBehavior;
import net.mehvahdjukaar.supplementaries.compat.create.behaviors.HourglassBehavior;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SupplementariesCreatePlugin {
    public static void initialize(){
        try {
            AllMovementBehaviours.addMovementBehaviour(Registry.BAMBOO_SPIKES.get(), new BambooSpikesBehavior());
            AllMovementBehaviours.addMovementBehaviour(Registry.HOURGLASS.get(), new HourglassBehavior());
        }catch (Exception e){
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: "+e);
        }
    }


}
