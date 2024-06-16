package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.AlternativeBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.GenericProjectileBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.SlingshotProjectileBehavior;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CannonBlock extends DirectionalBlock implements EntityBlock, ILightable, IRotatable {

    private static final Map<Item, IFireItemBehavior> FIRE_BEHAVIORS = new HashMap<>();
    private static final IFireItemBehavior DEFAULT =
            new AlternativeBehavior(new GenericProjectileBehavior(), new SlingshotProjectileBehavior());

    protected static final VoxelShape SHAPE_DOWN = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape SHAPE_UP = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    protected static final VoxelShape SHAPE_EAST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_WEST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CannonBlock(Properties properties) {
        super(properties);
    }

    public static void registerBehavior(ItemLike pItem, IFireItemBehavior pBehavior) {
        FIRE_BEHAVIORS.put(pItem.asItem(), pBehavior);
    }

    public static IFireItemBehavior getCannonBehavior(ItemLike item) {
        return FIRE_BEHAVIORS.getOrDefault(item, DEFAULT);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, POWERED);
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
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof CannonBlockTile cannon) {
            Direction dir = Direction.orderedByNearest(placer)[0];
            Direction myDir = state.getValue(FACING).getOpposite();

            if (dir.getAxis() == Direction.Axis.Y) {
                float pitch = dir == Direction.UP ? -90 : 90;
                cannon.setRestrainedPitch((myDir.getOpposite() == dir ? pitch + 180 : pitch));

            } else {
                float yaw = dir.toYRot();
                cannon.setRestrainedYaw((myDir.getOpposite() == dir ? yaw + 180 : yaw));
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
                    tile.ignite(null);
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
        return Utils.getTicker(pBlockEntityType, ModRegistry.CANNON_TILE.get(), CannonBlockTile::tick);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            tile.use(player, hand, hit);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof CannonBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }


    @Override
    public boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            return tile.isFiring();
        }
        return false;
    }

    @Override
    public boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world, FireSourceType fireSourceType) {
        if (world.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            if (tile.readyToFire()) {
                if (!world.isClientSide()) {
                    this.setLitUp(state, world, pos, true);
                    this.playLightUpSound(world, pos, fireSourceType);
                }

                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return true;
            } else {
                return false;
            }
        }
        return false;
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getShape(state, level, pos, context);
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
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id > 1) return false;
        if (!level.isClientSide) return true;
        if (level.getBlockEntity(pos) instanceof CannonBlockTile tile) {
            float yaw = tile.getYaw();
            float pitch = tile.getPitch();

            PoseStack poseStack = new PoseStack();
            poseStack.translate(pos.getX() + 0.5f, pos.getY() + 0.5f + 1 / 16f, pos.getZ() + 0.5f);

            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0, 0, -1.4);

            if (id == 1) {
                addFireParticles(pos, level, poseStack, pitch, yaw);
                return true;
            } else {
                Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1.75f, 1));

                level.addParticle(ParticleTypes.FLAME,
                        p.x, p.y, p.z, 0, 0, 0);
            }
        }

        return false;
    }


    private void addFireParticles(BlockPos pos, Level level, PoseStack poseStack, float pitch, float yaw) {
        level.addParticle(ModParticles.CANNON_FIRE_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                pitch * Mth.DEG_TO_RAD, -yaw * Mth.DEG_TO_RAD, 0);
        RandomSource ran = level.random;

        this.spawnDustRing(level, poseStack);
        this.spawnSmokeTrail(level, poseStack, ran);
    }

    private void spawnSmokeTrail(Level level, PoseStack poseStack, RandomSource ran) {
        int smokeCount = 40;
        for (int i = 0; i < smokeCount; i += 1) {

            poseStack.pushPose();

            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0, -MthUtils.nextWeighted(ran, 0.5f, 1, 0.06f), 0));

            float aperture = 0.5f;
            poseStack.translate(-aperture / 2 + ran.nextFloat() * aperture, -aperture / 2 + ran.nextFloat() * aperture, 0);

            Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1, 1));

            level.addParticle(ParticleTypes.SMOKE,
                    p.x, p.y, p.z,
                    speed.x, speed.y, speed.z);
            poseStack.popPose();
        }
    }

    private void spawnDustRing(Level level, PoseStack poseStack) {
        poseStack.pushPose();

        Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1, 1));

        int dustCount = 16;
        for (int i = 0; i < dustCount; i += 1) {

            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(90));

            poseStack.mulPose(Axis.XP.rotationDegrees(380f * i / dustCount));
            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0, 0.05f, 0));
            level.addParticle(ModParticles.BOMB_SMOKE_PARTICLE.get(),
                    p.x, p.y, p.z,
                    speed.x, speed.y, speed.z);
            poseStack.popPose();
        }

        poseStack.popPose();
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
            Quaternionf q = new Quaternionf().rotateAxis(angle*Mth.DEG_TO_RAD, axis.step());
            currentDir.rotate(q);
            Vec3 newDir = new Vec3(currentDir);
            tile.setRestrainedYaw((float) MthUtils.getYaw(newDir));
            tile.setRestrainedPitch((float) MthUtils.getPitch(newDir));
            tile.setChanged();
            tile.getLevel().sendBlockUpdated(pos, oldState, newState, 3);
        }
    }


}
