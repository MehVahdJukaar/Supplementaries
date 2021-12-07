package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.HangingSignGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HangingSignBlock extends SwayingBlock implements EntityBlock {
    protected static final VoxelShape SHAPE_Z = Block.box(7, 0, 0, 9, 16, 16);
    protected static final VoxelShape SHAPE_X = Block.box(0, 0, 7, 16, 16, 9);

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty TILE = BlockProperties.TILE; // is it renderer by tile entity? animated part

    public HangingSignBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENSION, 0).setValue(FACING, Direction.NORTH).setValue(TILE, false).setValue(HANGING, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof HangingSignBlockTile tile && tile.isAccessibleBy(player)) {
            ItemStack handItem = player.getItemInHand(handIn);
            boolean server = !level.isClientSide();

            InteractionResult result = tile.textHolder.playerInteract(level, pos, player, handIn, tile::setChanged);
            if(result != InteractionResult.PASS) return result;

            //place item
            //TODO: fix left hand(shield)
            if (handIn == InteractionHand.MAIN_HAND) {
                //remove
                if (!tile.isEmpty() && handItem.isEmpty()) {
                    ItemStack it = tile.removeStackFromSlot(0);
                    if (!level.isClientSide()) {
                        player.setItemInHand(handIn, it);
                        tile.setChanged();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                //place
                else if (!handItem.isEmpty() && tile.isEmpty()) {
                    ItemStack it = handItem.copy();
                    it.setCount(1);
                    tile.setItems(NonNullList.withSize(1, it));

                    if (!player.isCreative()) {
                        handItem.shrink(1);
                    }
                    if (!level.isClientSide()) {
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.10F + 0.95F);
                        tile.setChanged();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }

                // open gui (edit sign with empty hand)
                else if (handItem.isEmpty()) {
                    if (!server) tile.openScreen(level, pos, player);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if (state.getValue(HANGING)) {
            return worldIn.getBlockState(pos.above()).isFaceSturdy(worldIn, pos.above(), Direction.DOWN);
        } else {
            return worldIn.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).getMaterial().isSolid();
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        if (facing == Direction.UP) {
            return !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.defaultBlockState()
                    : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        } else {
            return facing == stateIn.getValue(FACING).getOpposite() ? !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.defaultBlockState()
                    : getConnectedState(stateIn, facingState, worldIn, facingPos) : stateIn;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HANGING) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING, TILE);
    }

    //TODO: merge with lantern
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        if (context.getClickedFace() == Direction.DOWN || context.getClickedFace() == Direction.UP) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getCounterClockWise())
                    .setValue(HANGING, context.getClickedFace() == Direction.DOWN).setValue(WATERLOGGED, water);
        }
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        BlockPos relative = blockpos.relative(context.getClickedFace().getOpposite());
        BlockState facingState = world.getBlockState(relative);

        return getConnectedState(this.defaultBlockState(), facingState, world, relative).setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, water);
    }

    //for player bed spawn
    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
        return BlockPathTypes.OPEN;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof HangingSignBlockTile tile) {
                //InventoryHelper.dropInventoryItems(world, pos, (HangingSignBlockTile) tileentity);

                ItemStack itemstack = tile.getStackInSlot(0);
                ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);
                itementity.setDefaultPickUpDelay();
                world.addFreshEntity(itementity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HangingSignBlockTile(pPos, pState);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }


    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        return List.of(new ItemStack(this));
    }
}

