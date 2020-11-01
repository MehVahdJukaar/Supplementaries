package net.mehvahdjukaar.supplementaries.blocks;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class WindVaneBlock extends Block{
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED; // is it rooster only?
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    public WindVaneBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(INVERTED, false).with(POWER, 0));
    }

    public static void updatePower(BlockState bs, World world, BlockPos pos) {
        int weather = 0;
        if (world.isThundering()) {
            weather = 2;
        } else if (world.isRaining()) {
            weather = 1;
        }
        if (weather != bs.get(POWER)) {
            world.setBlockState(pos, bs.with(POWER, weather), 3);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        return blockState.get(POWER);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER, INVERTED);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.create(0.125D, 0D, 0.125D, 0.875D, 1D, 0.875D);
    }

    /*
     * @Override public PathNodeType getAiPathNodeType(BlockState state,
     * IBlockReader world, BlockPos pos, MobEntity entity) { return
     * PathNodeType.WALKABLE; }
     */

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WindVaneBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}