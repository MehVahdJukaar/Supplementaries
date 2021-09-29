package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BookPileHorizontalBlock extends BookPileBlock {

    private static final VoxelShape SHAPE_1_Z = Block.box(6D, 0D, 4D, 10D, 10D, 12D);
    private static final VoxelShape SHAPE_1_X = Block.box(4D, 0D, 6D, 12D, 10D, 10D);

    private static final VoxelShape SHAPE_2_Z = Block.box(3D, 0D, 4D, 13D, 10D, 12D);
    private static final VoxelShape SHAPE_2_X = Block.box(4D, 0D, 3D, 12D, 10D, 13D);


    private static final VoxelShape SHAPE_3_Z = Block.box(1D, 0D, 4D, 15D, 10D, 12D);
    private static final VoxelShape SHAPE_3_X = Block.box(4D, 0D, 2D, 12D, 10D, 14D);


    private static final VoxelShape SHAPE_4_Z = Block.box(0D, 0D, 4D, 16D, 10D, 12D);
    private static final VoxelShape SHAPE_4_X = Block.box(4D, 0D, 0D, 12D, 10D, 16D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BookPileHorizontalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(BOOKS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            return blockstate.setValue(BOOKS, blockstate.getValue(BOOKS) + 1);
        }
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
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

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BookPileBlockTile(true);
    }

    public boolean isAcceptedItem(Item i){
        return i == Items.BOOK;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        boolean x = state.getValue(FACING).getAxis()== Direction.Axis.X;

        switch (state.getValue(BOOKS)){
            default:
            case 1:
                return x ? SHAPE_1_X : SHAPE_1_Z;
            case 2:
                return x ? SHAPE_2_X : SHAPE_2_Z;
            case 3:
                return x ? SHAPE_3_X : SHAPE_3_Z;
            case 4:
                return x ? SHAPE_4_X : SHAPE_4_Z;
        }
    }
}
