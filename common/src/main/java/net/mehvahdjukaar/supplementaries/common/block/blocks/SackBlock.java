package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SackBlock extends FallingBlock implements EntityBlock {

    public static final VoxelShape SHAPE_CLOSED = Shapes.or(Block.box(2, 0, 2, 14, 12, 14),
            Block.box(6, 12, 6, 10, 13, 10), Block.box(5, 13, 5, 11, 16, 11));
    public static final VoxelShape SHAPE_OPEN = Shapes.or(Block.box(2, 0, 2, 14, 12, 14),
            Block.box(6, 12, 6, 10, 13, 10), Block.box(3, 13, 3, 13, 14, 13));


    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public SackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(WATERLOGGED, false));
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return 0xba8f6a;
    }

    //falling block
    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() != oldState.getBlock()) {
            worldIn.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    //@Override
    //protected void onStartFalling(FallingBlockEntity fallingEntity) { fallingEntity.setHurtEntities(true); }

    public static boolean canFall(BlockPos pos, LevelAccessor world) {
        return (world.isEmptyBlock(pos.below()) || isFree(world.getBlockState(pos.below()))) &&
                pos.getY() >= world.getMinBuildHeight() && !RopeBlock.isSupportingCeiling(pos.above(), world);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (level.getBlockEntity(pos) instanceof SackBlockTile tile) {
            tile.recheckOpen();
            if (canFall(pos, level)) {
                ImprovedFallingBlockEntity entity = ImprovedFallingBlockEntity.fall(ModRegistry.FALLING_SACK.get(),
                        level, pos, state, true);
                entity.blockData = tile.saveWithoutMetadata();
                entity.setHurtsEntities(1, 20);
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SackBlockTile(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {

                player.openMenu(tile);
                PiglinAi.angerNearbyPiglins(player, true);

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    //for creative drop
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
            if (!worldIn.isClientSide && player.isCreative() && !tile.isEmpty()) {
                CompoundTag compoundTag = tile.saveWithoutMetadata();
                ItemStack itemstack = new ItemStack(this);
                if (!compoundTag.isEmpty()) {
                    itemstack.addTagElement("BlockEntityTag", compoundTag);
                }

                if (tile.hasCustomName()) {
                    itemstack.setHoverName(tile.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SackBlockTile tile) {
            builder = builder.withDynamicDrop(CONTENTS, (context, stackConsumer) -> {
                for (int i = 0; i < tile.getContainerSize(); ++i) {
                    stackConsumer.accept(tile.getItem(i));
                }
            });
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof SackBlockTile tile) {
            CompoundTag compoundTag = tile.saveWithoutMetadata();
            if (!compoundTag.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundTag);
            }
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(OPEN))
            return SHAPE_OPEN;
        return SHAPE_CLOSED;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
            int i = 0;
            float f = 0.0F;
            int slots = tile.getUnlockedSlots();
            for (int j = 0; j < slots; ++j) {
                ItemStack itemstack = tile.getItem(j);
                if (!itemstack.isEmpty()) {
                    f += (float) itemstack.getCount() / (float) Math.min(tile.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }
            f = f / (float) slots;
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState state1, FallingBlockEntity blockEntity) {
        super.onLand(level, pos, state, state1, blockEntity);
        //land sound
        if (!blockEntity.isSilent()) {
            level.playSound(null, pos, state.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

}
