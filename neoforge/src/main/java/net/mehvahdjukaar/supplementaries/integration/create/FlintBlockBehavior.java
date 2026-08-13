package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlintBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// lets a flint block on a contraption spark against metal it scrapes past, same as when a piston
// pushes metal into it
public class FlintBlockBehavior implements MovementBehaviour {

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        Level level = context.world;
        if (level == null || level.isClientSide) return;
        // the flint block now occupies 'pos'; it can only spark where there is air to place fire into
        if (!level.getBlockState(pos).isAir()) return;
        for (Direction dir : Direction.values()) {
            BlockPos metalPos = pos.relative(dir);
            BlockState neighbor = level.getBlockState(metalPos);
            // the metal face pointing back toward the flint block is dir.getOpposite()
            if (FlintBlock.canBlockCreateSpark(neighbor, level, metalPos, dir.getOpposite())) {
                FlintBlock.ignitePosition(level, pos, true);
                return;
            }
        }
    }
}
