package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.TomatoBlock;

/**
 * A tomato crop sitting on a stick. Created when a vine climbs into our stick block (and when a stick
 * is added onto a tomato, see {@link FarmersDelightCompat#canAddStickToTomato}). The inherited
 * {@code ROPELOGGED} property is left unused.
 */
public class TomatoStickBlock extends TomatoBlock {

    public static final BooleanProperty AXIS_X = ModBlockProperties.AXIS_X;
    public static final BooleanProperty AXIS_Z = ModBlockProperties.AXIS_Z;

    public TomatoStickBlock(Properties properties) {
        super(properties);
        // ROPELOGGED is inherited from TomatoBlock but unused here; kept false so it stays inert
        this.registerDefaultState(this.defaultBlockState().setValue(ROPELOGGED, false)
                .setValue(AXIS_X, false).setValue(AXIS_Z, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS_X, AXIS_Z);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return StickBlock.getStickShape(state.getValue(AXIS_X), true, state.getValue(AXIS_Z));
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
