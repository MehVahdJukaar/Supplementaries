package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class EnhancedLanternBlock extends SwayingBlock implements EntityBlock {
    public static final VoxelShape SHAPE_SOUTH = Block.box(5, 2, 0, 11, 15.99, 10);
    public static final VoxelShape SHAPE_NORTH = Block.box(5, 2, 6, 11, 15.99, 16);
    public static final VoxelShape SHAPE_WEST = Block.box(6, 2, 5, 16, 15.99, 11);
    public static final VoxelShape SHAPE_EAST = Block.box(0, 2, 5, 10, 15.99, 11);

    public EnhancedLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(FACING, Direction.NORTH).setValue(EXTENSION, 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.wall_lantern").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));

    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() == Direction.UP || context.getClickedFace() == Direction.DOWN) return null;
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        BlockPos relative = blockpos.relative(context.getClickedFace().getOpposite());
        BlockState facingState = world.getBlockState(relative);

        boolean flag = world.getFluidState(blockpos).getType() == Fluids.WATER;

        return getConnectedState(this.defaultBlockState(), facingState, world, relative).setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, flag);
    }

    public void placeOn(BlockState lantern, BlockPos onPos, Direction face, Level world) {
        BlockState state = getConnectedState(this.defaultBlockState(), world.getBlockState(onPos), world, onPos)
                .setValue(FACING, face);
        BlockPos newPos = onPos.relative(face);
        world.setBlock(newPos, state, 3);
        if (world.getBlockEntity(newPos) instanceof IBlockHolder tile) {
            tile.setHeldBlock(lantern);
        }
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return (blockstate.isFaceSturdy(worldIn, blockpos, direction) || CommonUtil.getPostSize(blockstate, blockpos, worldIn) > 0);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EnhancedLanternBlockTile(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> SHAPE_SOUTH;
            case NORTH -> SHAPE_NORTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }


}
