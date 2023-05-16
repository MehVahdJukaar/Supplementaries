package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.integration.forge.CreateCompatImpl;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

//TODO: fix
public class PulleyBehavior implements MovementBehaviour {

    private static final PulleyBlockTile DUMMY = new PulleyBlockTile(BlockPos.ZERO, ModRegistry.PULLEY_BLOCK.get().defaultBlockState());


    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        BlockState state = context.state;
        var axis = state.getValue(PulleyBlock.AXIS);
        if (axis == Direction.Axis.Y) return;
        CreateCompatImpl.changeState(context, state.cycle(PulleyBlock.FLIPPED));
        Direction dir = null;
        var center = context.contraption.anchor;
        if (axis == Direction.Axis.X) {
            dir = Direction.NORTH;
        } else if (axis == Direction.Axis.Z) {
            dir = Direction.WEST;
        }
        if (dir == null) return;

        DUMMY.load(context.tileData);
        DUMMY.setLevel(context.world);

        Rotation rot = context.relativeMotion.length() > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
        DUMMY.handleRotation(rot, pos);
        context.tileData = DUMMY.saveWithFullMetadata();
    }

}
