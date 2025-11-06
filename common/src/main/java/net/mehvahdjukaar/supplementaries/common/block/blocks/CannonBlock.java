package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.AlternativeBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.GenericProjectileBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.SlingshotBehavior;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CannonBlock extends DirectionalBlock implements EntityBlock, ILightable, IRotatable {

    public static final int MAX_POWER_LEVELS = 4;
    public static final MapCodec<CannonBlock> CODEC = simpleCodec(CannonBlock::new);

    private static final Map<Item, IFireItemBehavior> FIRE_BEHAVIORS = new Object2ObjectOpenHashMap<>();
    private static final IFireItemBehavior DEFAULT = new AlternativeBehavior(
            new GenericProjectileBehavior(), new SlingshotBehavior());

    protected static final VoxelShape SHAPE_DOWN = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape SHAPE_UP = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    protected static final VoxelShape SHAPE_EAST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_WEST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Rotation> ROTATE_TILE = ModBlockProperties.ROTATE_TILE;

    public CannonBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(ROTATE_TILE, Rotation.NONE)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    public static void clearBehaviors() {
        FIRE_BEHAVIORS.clear();
    }

    public static void registerBehavior(ItemLike pItem, IFireItemBehavior pBehavior) {
        FIRE_BEHAVIORS.put(pItem.asItem(), pBehavior);
    }

    public static IFireItemBehavior getCannonBehavior(ItemLike item) {
        return FIRE_BEHAVIORS.getOrDefault(item, DEFAULT);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (!MiscUtils.showsHints(tooltipFlag)) return;
        tooltipComponents.add((Component.translatable("message.supplementaries.cannon")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            return tile;
        }
        return null;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, POWERED, ROTATE_TILE);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)))
                .setValue(ROTATE_TILE, state.getValue(ROTATE_TILE).getRotated(rot));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
              //  .setValue(ROTATE_TILE, mirrorIn.ro(Rotation.CLOCKWISE_180)); //todo: bug here
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof CannonBlockTile cannon) {
            Direction dir = Direction.orderedByNearest(placer)[0];
            Direction myDir = state.getValue(FACING).getOpposite();

            if (dir.getAxis() == Direction.Axis.Y) {
                float pitch = dir == Direction.UP ? -90 : 90;
                cannon.setPitch(cannon.selfAccess, (myDir.getOpposite() == dir ? pitch + 180 : pitch));

            } else {
                float yaw = dir.toYRot();
                cannon.setYaw(cannon.selfAccess, (myDir.getOpposite() == dir ? yaw + 180 : yaw));
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean wasPowered = state.getValue(POWERED);
            if (wasPowered != level.hasNeighborSignal(pos)) {
                level.setBlock(pos, state.cycle(POWERED), 2);
                if (!wasPowered && level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
                    tile.ignite(null, tile.selfAccess);
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CannonBlockTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, ModRegistry.CANNON_TILE.get(), CannonBlock::tickTile);
    }

    private static void tickTile(Level level, BlockPos pos, BlockState state, CannonBlockTile cannonBlockTile) {
        cannonBlockTile.tick(cannonBlockTile.selfAccess);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var r = this.lightableInteractWithPlayerItem(state, level, pos, player, hand, stack);
        if (r.consumesAction()) return r;
        if (level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            if (player instanceof ServerPlayer sp) {
                tile.tryOpeningEditGui(sp, pos, stack, hitResult.getDirection());
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            return tile.isFiring();
        }
        return false;
    }

    @Override
    public boolean tryLightUp(@Nullable Entity player, BlockState state, BlockPos pos,
                              LevelAccessor world, FireSoundType fireSourceType) {
        if (world.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            if (!tile.readyToFire()) return false;
        }
        return ILightable.super.tryLightUp(player, state, pos, world, fireSourceType);
    }

    @Override
    public void setLitUp(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos,
                         @Nullable Entity igniter, boolean on) {
        if (levelAccessor.getBlockEntity(blockPos) instanceof CannonBlockTile tile) {
            tile.ignite(igniter, tile.selfAccess);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (state.getValue(FACING).getOpposite()) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ec) {
            if (ec.getEntity() instanceof Projectile p && p.tickCount < 10) {
                return Shapes.empty();
            } else if (ec.getEntity() != null) {
                return super.getCollisionShape(state, level, pos, context);
            }
        }
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        int power = 0;
        if (level.getBlockEntity(blockPos) instanceof CannonBlockTile tile) {
            if (tile.isOnCooldown() || tile.isFiring()) {
                power += 15;
            }
            if (!tile.getProjectile().isEmpty()) {
                power += 8;
            }
            ItemStack ammo = tile.getFuel();
            int maxStackSize = ammo.getMaxStackSize();
            int ammoCount = ammo.getCount();
            int importantCount = Math.min(ammoCount, MAX_POWER_LEVELS);
            power += importantCount;
            int nonImportantCount = maxStackSize - MAX_POWER_LEVELS;
            float remainingPower = Mth.map(nonImportantCount, 0, maxStackSize - MAX_POWER_LEVELS,
                    0, 3);
            if (remainingPower > 0) {
                power += (int) remainingPower;
            }
        }
        return Mth.clamp(power, 0, 15);
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor levelAccessor, BlockPos blockPos,
                                                Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        boolean ccw = rotation == Rotation.COUNTERCLOCKWISE_90;
        return BlockUtil.getRotatedDirectionalBlock(state, axis, ccw).or(() -> Optional.of(state));
    }

    @Override
    public void onRotated(BlockState newState, BlockState oldState, LevelAccessor world, BlockPos pos, Rotation rotation,
                          Direction axis, @Nullable Vec3 hit) {
        if (axis.getAxis() == newState.getValue(FACING).getAxis() && world.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            float angle = rotation.rotate(0, 4) * -90;
            Vector3f currentDir = Vec3.directionFromRotation(tile.getPitch(), tile.getYaw()).toVector3f();
            Quaternionf q = new Quaternionf().rotateAxis(angle * Mth.DEG_TO_RAD, axis.step());
            currentDir.rotate(q);
            Vec3 newDir = new Vec3(currentDir);
            tile.setYaw(tile.selfAccess, (float) MthUtils.getYaw(newDir));
            tile.setPitch(tile.selfAccess, (float) MthUtils.getPitch(newDir));
            tile.setChanged();
            tile.getLevel().sendBlockUpdated(pos, oldState, newState, 3);
        }
    }


}
