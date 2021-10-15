package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class EnhancedLanternBlock extends SwayingBlock {
    public static final VoxelShape SHAPE_SOUTH = Shapes.box(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
    public static final VoxelShape SHAPE_NORTH = Shapes.box(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
    public static final VoxelShape SHAPE_WEST = Shapes.box(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
    public static final VoxelShape SHAPE_EAST = Shapes.box(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);

    public EnhancedLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false)
                .setValue(FACING,Direction.NORTH).setValue(EXTENSION,0));
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)return;
        tooltip.add(new TranslatableComponent("message.supplementaries.wall_lantern").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));

    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() == Direction.UP || context.getClickedFace() == Direction.DOWN) return null;
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        BlockPos facingpos = blockpos.relative(context.getClickedFace().getOpposite());
        BlockState facingState = world.getBlockState(facingpos);

        boolean flag = world.getFluidState(blockpos).getType() == Fluids.WATER;;

        return getConnectedState(this.defaultBlockState(), facingState, world, facingpos).setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED,flag);
    }

    public void placeOn(BlockState lantern, BlockPos onPos, Direction face, Level world){
        BlockState state = getConnectedState(this.defaultBlockState(),world.getBlockState(onPos),world,onPos)
                .setValue(FACING,face);
        BlockPos newPos = onPos.relative(face);
        world.setBlock(newPos,state,3);
        BlockEntity te = world.getBlockEntity(newPos);
        if(te instanceof IBlockHolder){
            ((IBlockHolder) te).setHeldBlock(lantern);
        }
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return (blockstate.isFaceSturdy(worldIn, blockpos, direction) || CommonUtil.getPostSize(blockstate,blockpos, worldIn)>0);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new EnhancedLanternBlockTile();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
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
