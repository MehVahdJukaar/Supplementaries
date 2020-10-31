package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;


public class PlanterBlock extends Block {
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // raised dirt?

    public PlanterBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(EXTENDED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.updatedState(this.getDefaultState(), context.getWorld(), context.getPos());
    }

    //called when a neighbor is placed
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if(facing==Direction.UP){
            return this.updatedState(stateIn, (World) worldIn, currentPos);
        }
        return stateIn;
    }

    /*
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        if(fromPos.equals(pos.up())) world.setBlockState(pos, this.updatedState(state, world, pos));
    }*/

    public BlockState updatedState(BlockState state, World world, BlockPos pos){
        return state.with(EXTENDED, this.canConnect(world.getBlockState(pos.up()).getBlock()));
    }

    public boolean canConnect(Block block){
        return !((block instanceof AirBlock) || (block instanceof StemBlock) || (block instanceof CropsBlock));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.or(VoxelShapes.create(0.125D, 0D, 0.125D, 0.875D, 0.687D, 0.875D), VoxelShapes.create(0D, 0.687D, 0D, 1D, 1D, 1D));
    }
}