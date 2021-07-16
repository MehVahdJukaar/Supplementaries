package net.mehvahdjukaar.supplementaries.compat.create.behaviors;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.curiosities.bell.AbstractBellBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.PulleyBlockTile;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PulleyBlockBehavior extends MovementBehaviour {

    PulleyBlockTile tempTile = new PulleyBlockTile();

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        World world = context.world;
        BlockState stateVisited = world.getBlockState(pos);
        tempTile.load(context.state, context.tileData);
    }

}
