package net.mehvahdjukaar.supplementaries.integration.farmersdelight;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TomatoStickBlock extends TomatoLoggedBlock {

    public static final BooleanProperty AXIS_X = BlockProperties.AXIS_X;
    public static final BooleanProperty AXIS_Z = BlockProperties.AXIS_Z;

    public TomatoStickBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ROPELOGGED, true)
                .setValue(AXIS_X, false).setValue(AXIS_Z, false));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return StickBlock.getStickShape(state.getValue(AXIS_X), true, state.getValue(AXIS_Z));
    }

    public Block getInnerBlock() {
        return ModRegistry.STICK_BLOCK.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS_X, AXIS_Z);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
                                  BlockPos facingPos) {
        super.updateShape(state, facing, facingState, world, currentPos, facingPos);

        if (facing == Direction.DOWN && !world.isClientSide()) {
            FDCompatRegistry.tryTomatoLogging(facingState, world, facingPos, false);
        }
        return state;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().is(Items.STICK)) {
            return switch (context.getClickedFace().getAxis()) {
                case Z -> !state.getValue(AXIS_Z);
                case X -> !state.getValue(AXIS_X);
                default -> false;
            };
        }
        return super.canBeReplaced(state, context);
    }
}
