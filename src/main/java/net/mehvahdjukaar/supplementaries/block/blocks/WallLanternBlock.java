package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


public class WallLanternBlock extends SwayingBlock {
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.create(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);

    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;
    public WallLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(LIGHT_LEVEL, 0).with(WATERLOGGED,false));
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add((new StringTextComponent("You shouldn't have this")).mergeStyle(TextFormatting.GRAY));
    }

    //update light level. TODO: maybe use this to update jars (onBlockAdded)


    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        /*
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return ((WallLanternBlockTile) te).lanternBlock.getLightValue();
        }*/

        return state.get(LIGHT_LEVEL);
    }

    //TODO: replace getItem with getPickBlock
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return new ItemStack(((WallLanternBlockTile) te).lanternBlock.getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return super.getItem(worldIn, pos, state);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return (blockstate.isSolidSide(worldIn, blockpos, direction) || CommonUtil.getPostSize(blockstate,blockpos, worldIn)>0);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            case UP :
            case DOWN :
            case SOUTH :
            default :
                return SHAPE_SOUTH;
            case NORTH :
                return SHAPE_NORTH;
            case WEST :
                return SHAPE_WEST;
            case EAST :
                return SHAPE_EAST;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(LIGHT_LEVEL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN) return null;
        BlockPos blockpos = context.getPos();
        World world = context.getWorld();
        BlockPos facingpos = blockpos.offset(context.getFace().getOpposite());
        BlockState facingState = world.getBlockState(facingpos);

        boolean flag = world.getFluidState(blockpos).getFluid() == Fluids.WATER;;

        return this.getConnectedState(this.getDefaultState(), facingState, world, facingpos).with(FACING, context.getFace()).with(WATERLOGGED,flag);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof WallLanternBlockTile){
            return Collections.singletonList(new ItemStack(((WallLanternBlockTile) tileentity).lanternBlock.getBlock()));
        }
        return super.getDrops(state,builder);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WallLanternBlockTile();
    }

}