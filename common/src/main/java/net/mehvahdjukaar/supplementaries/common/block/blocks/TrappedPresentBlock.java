package net.mehvahdjukaar.supplementaries.common.block.blocks;


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.common.block.util.IPresentItemBehavior;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrappedPresentBlock extends WaterBlock implements EntityBlock, IColored {

    private static final Map<Item, IPresentItemBehavior> TRAPPED_PRESENT_INTERACTIONS_REGISTRY = Util.make(new Object2ObjectOpenHashMap<>(),
            (map) -> map.defaultReturnValue(((source, stack) -> Optional.empty())));

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PRIMED = BlockProperties.PACKED;

    private final DyeColor color;

    public TrappedPresentBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(PRIMED, false)
                .setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
    }

    public static void registerBehavior(ItemLike pItem, IPresentItemBehavior pBehavior) {
        TRAPPED_PRESENT_INTERACTIONS_REGISTRY.put(pItem.asItem(), pBehavior);
    }

    public static IPresentItemBehavior getPresentBehavior(ItemStack pStack) {
        return TRAPPED_PRESENT_INTERACTIONS_REGISTRY.get(pStack.getItem());
    }


    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PRIMED, WATERLOGGED, FACING);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TrappedPresentBlockTile(pPos, pState);
    }


    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof TrappedPresentBlockTile tile && player instanceof ServerPlayer serverPlayer) {
                return tile.interact(serverPlayer, pos);
            }
        }
        return InteractionResult.PASS;
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof TrappedPresentBlockTile tile) {
            if (!worldIn.isClientSide && player.isCreative() && !tile.isEmpty()) {
                ItemStack itemstack = tile.getPresentItem(this);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //normal drop
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TrappedPresentBlockTile tile) {
            ItemStack itemstack = tile.getPresentItem(this);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof TrappedPresentBlockTile tile) {
            return tile.getPresentItem(this);
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof TrappedPresentBlockTile tile) {
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
        if (state.getValue(PRIMED))
            return PresentBlock.SHAPE_CLOSED;
        return PresentBlock.SHAPE_OPEN;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider menu ? menu : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        CompoundTag tag = context.getItemInHand().getTag();
        if (tag != null && tag.contains("BlockEntityTag")) {
            CompoundTag t = tag.getCompound("BlockEntityTag");
            if (t.contains("Items")) state = state.setValue(PRIMED, true);
        }
        return state.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        if (pId == 0) {
            if (pLevel.isClientSide) {
                RandomSource random = pLevel.random;


                double cx = (double) pPos.getX() + 0.5D;
                double cy = (double) pPos.getY() + 0.5F + 0.4;
                double cz = (double) pPos.getZ() + 0.5D;

                for (int i = 0; i < 10; ++i) {
                    double speed = random.nextDouble() * 0.15D + 0.015D;
                    double py = cy + 0.02D + (random.nextDouble() - 0.5D) * 0.3D;
                    double dx = random.nextGaussian() * 0.01D;
                    double dy = speed + random.nextGaussian() * 0.01D;
                    double dz = random.nextGaussian() * 0.01D;
                    pLevel.addParticle(ParticleTypes.CLOUD, cx, py, cz, dx, dy, dz);
                }

                this.destroyLid(pPos, pState, pLevel);

                // ((ClientLevel)pLevel).playLocalSound(pPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 0.7f,false);
            }
            return true;
        }
        return super.triggerEvent(pState, pLevel, pPos, pId, pParam);
    }

    @Environment(EnvType.CLIENT)
    public void destroyLid(BlockPos pPos, BlockState pState, Level level) {
        var particleEngine = Minecraft.getInstance().particleEngine;
        VoxelShape voxelshape = PresentBlock.SHAPE_LID;

        voxelshape.forAllBoxes((p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
            double d1 = Math.min(1.0D, p_172276_ - p_172273_);
            double d2 = Math.min(1.0D, p_172277_ - p_172274_);
            double d3 = Math.min(1.0D, p_172278_ - p_172275_);
            int i = Math.max(2, Mth.ceil(d1 / 0.25D));
            int j = Math.max(2, Mth.ceil(d2 / 0.25D));
            int k = Math.max(2, Mth.ceil(d3 / 0.25D));

            for (int l = 0; l < i; ++l) {
                for (int i1 = 0; i1 < j; ++i1) {
                    for (int j1 = 0; j1 < k; ++j1) {
                        double d4 = ((double) l + 0.5D) / (double) i;
                        double d5 = ((double) i1 + 0.5D) / (double) j;
                        double d6 = ((double) j1 + 0.5D) / (double) k;
                        double d7 = d4 * d1 + p_172273_;
                        double d8 = d5 * d2 + p_172274_;
                        double d9 = d6 * d3 + p_172275_;
                        particleEngine.add(new TerrainParticle((ClientLevel) level, (double) pPos.getX() + d7, (double) pPos.getY() + d8, (double) pPos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, pState, pPos));
                    }
                }
            }

        });

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
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_, boolean p_220069_6_) {
        super.neighborChanged(state, world, pos, p_220069_4_, p_220069_5_, p_220069_6_);
        boolean isPowered = world.hasNeighborSignal(pos);
        if (world instanceof ServerLevel serverLevel && isPowered && state.getValue(PRIMED)
                && world.getBlockEntity(pos) instanceof TrappedPresentBlockTile tile) {
            tile.detonate(serverLevel, pos);
        }
    }
}
