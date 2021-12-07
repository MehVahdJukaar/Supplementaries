package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.DoormatBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.DoormatGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DoormatBlock extends WaterBlock implements EntityBlock{
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 2.0D, 16.0D, 1.0D, 14.0D);

    protected static final VoxelShape SHAPE_WEST = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 1.0D, 16.0D);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public DoormatBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof DoormatBlockTile tile && tile.isAccessibleBy(player)) {

            ItemStack itemstack = player.getItemInHand(handIn);
            boolean server = !level.isClientSide();

            boolean sideHit = hit.getDirection() != Direction.UP;
            boolean canExtract = itemstack.isEmpty() && (player.isShiftKeyDown() || sideHit);
            boolean canInsert = tile.isEmpty() && sideHit;
            if (canExtract ^ canInsert) {
                if (!server) return InteractionResult.SUCCESS;
                if (canExtract) {
                    ItemStack dropStack = tile.removeItemNoUpdate(0);
                    ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.125, pos.getZ() + 0.5, dropStack);
                    drop.setDefaultPickUpDelay();
                    level.addFreshEntity(drop);
                } else {
                    ItemStack newStack = itemstack.copy();
                    newStack.setCount(1);
                    tile.setItems(NonNullList.withSize(1, newStack));
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                }
                tile.setChanged();
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F,
                        1.2f);
                return InteractionResult.CONSUME;
            }
            //color
            InteractionResult result = tile.textHolder.playerInteract(level, pos, player, handIn, tile::setChanged);
            if(result != InteractionResult.PASS){
                return result;
            }
            // open gui (edit sign with empty hand)
            else if (!server) {
                tile.openScreen(level, pos, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !worldIn.isEmptyBlock(pos.below());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_WEST : SHAPE_NORTH;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
    }

    //for player bed spawn
    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }

//TODO: maybe make conditional tile
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DoormatBlockTile(pPos, pState);
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
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof Container tile) {
                Containers.dropContents(world, pos, tile);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }

}