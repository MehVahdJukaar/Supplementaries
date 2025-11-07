package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobeBlock extends WaterBlock implements EntityBlock, IWashable {
    protected static final VoxelShape SHAPE = Block.box(2, 0D, 2, 14, 15D, 14);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty ROTATION = ModBlockProperties.ROTATION_4;

    public GlobeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(POWERED, false).setValue(ROTATION, 0).setValue(FACING, Direction.NORTH));
    }

    public static void displayCurrentCoordinates(Level level, Player player, BlockPos pos) {
        String x = String.valueOf(pos.getX());
        String z = String.valueOf(pos.getZ());
        if (!level.dimensionType().natural()) {
            x = ChatFormatting.OBFUSCATED + x + ChatFormatting.RESET;
            z = ChatFormatting.OBFUSCATED + z + ChatFormatting.RESET;
        }
        player.displayClientMessage(Component.translatable("message.supplementaries.compass", x, z), true);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            if (worldIn.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    public void updatePower(BlockState state, Level leve, BlockPos pos) {
        boolean powered = leve.getBestNeighborSignal(pos) > 0;
        if (powered != state.getValue(POWERED)) {
            leve.setBlock(pos, state.setValue(POWERED, powered), 4);
            //server
            //calls event on server and client through packet
            if (powered) {
                leve.gameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
                leve.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof GlobeBlockTile tile&& tile.hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, tile.getCustomName());
        }
        return stack;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
            if (!level.isClientSide) {
                if (tile.isSpinningVeryFast() && player instanceof ServerPlayer serverPlayer) {
                    Utils.awardAdvancement(serverPlayer, Supplementaries.res("adventure/globe"));
                }

                level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
                level.blockEvent(pos, state.getBlock(), 1, 0);
                if (CommonConfigs.Building.GLOBE_COORDINATES.get()) {
                    displayCurrentCoordinates(level, player, pos);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof ShearsItem) {
            if (level.getBlockEntity(pos) instanceof GlobeBlockTile tile) {

                tile.toggleShearing();
                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                level.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1, 1);
                if (!level.isClientSide) {
                    level.blockEvent(pos, state.getBlock(), 2, 0);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
        if (MiscUtils.FESTIVITY.isEarthDay() && level.isClientSide) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            for (int l = 0; l < 1; ++l) {
                double d0 = (x + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                double d1 = (y + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                double d2 = (z + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                level.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0, 0, 0);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, POWERED, ROTATION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GlobeBlockTile(pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity tile = world.getBlockEntity(pos);
        return tile != null && tile.triggerEvent(eventID, eventParam);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
            return tile.getSignalPower();
        }
        return 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, ModRegistry.GLOBE_TILE.get(), GlobeBlockTile::tick);
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
        if (level.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
            if (tile.isSepia()) {
                level.setBlockAndUpdate(pos, ModRegistry.GLOBE.get().withPropertiesOf(state));
                return true;
            }
        }
        return false;
    }
}