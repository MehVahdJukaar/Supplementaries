package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BookPileHorizontalBlock extends BookPileBlock {

    private static final VoxelShape SHAPE_1_Z = Block.box(6D, 0D, 4D, 10D, 10D, 12D);
    private static final VoxelShape SHAPE_1_X = Block.box(4D, 0D, 6D, 12D, 10D, 10D);

    private static final VoxelShape SHAPE_2_Z = Block.box(3D, 0D, 4D, 13D, 10D, 12D);
    private static final VoxelShape SHAPE_2_X = Block.box(4D, 0D, 3D, 12D, 10D, 13D);


    private static final VoxelShape SHAPE_3_Z = Block.box(1D, 0D, 4D, 15D, 10D, 12D);
    private static final VoxelShape SHAPE_3_X = Block.box(4D, 0D, 1D, 12D, 10D, 15D);


    private static final VoxelShape SHAPE_4_Z = Block.box(0D, 0D, 4D, 16D, 10D, 12D);
    private static final VoxelShape SHAPE_4_X = Block.box(4D, 0D, 0D, 12D, 10D, 16D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BookPileHorizontalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(BOOKS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.getBlock() instanceof BookPileBlock) {
            return blockstate.setValue(BOOKS, blockstate.getValue(BOOKS) + 1);
        }
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidState.getType() == Fluids.WATER && fluidState.getAmount() == 8;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BookPileBlockTile(pPos, pState, true);
    }

    public boolean isAcceptedItem(Item i) {
        return isNormalBook(i) || (CommonConfigs.Tweaks.MIXED_BOOKS.get() && isEnchantedBook(i));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean x = state.getValue(FACING).getAxis() == Direction.Axis.X;

        return switch (state.getValue(BOOKS)) {
            default -> x ? SHAPE_1_X : SHAPE_1_Z;
            case 2 -> x ? SHAPE_2_X : SHAPE_2_Z;
            case 3 -> x ? SHAPE_3_X : SHAPE_3_Z;
            case 4 -> x ? SHAPE_4_X : SHAPE_4_Z;
        };
    }
}
