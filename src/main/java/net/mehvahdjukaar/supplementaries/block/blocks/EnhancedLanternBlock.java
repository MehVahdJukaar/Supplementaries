package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EnhancedLanternBlock extends SwayingBlock {
    public static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
    public static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
    public static final VoxelShape SHAPE_WEST = VoxelShapes.create(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
    public static final VoxelShape SHAPE_EAST = VoxelShapes.create(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);

    public EnhancedLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED,false)
                .with(FACING,Direction.NORTH).with(EXTENSION,0));
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().gameSettings.advancedItemTooltips)return;
        tooltip.add(new TranslationTextComponent("message.supplementaries.wall_lantern").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));

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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnhancedLanternBlockTile();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            case UP:
            case DOWN:
            case SOUTH:
            default:
                return SHAPE_SOUTH;
            case NORTH:
                return SHAPE_NORTH;
            case WEST:
                return SHAPE_WEST;
            case EAST:
                return SHAPE_EAST;
        }
    }
}
