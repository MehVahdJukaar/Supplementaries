package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

//TODO: extinguish sound. same for candle skull & interaction
public class CandleHolderBlock extends LightUpWaterBlock implements IColored {
    protected static final VoxelShape SHAPE_FLOOR = Block.box(5D, 0D, 5D, 11D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_NORTH = Block.box(5D, 0D, 11D, 11D, 14D, 16D);
    protected static final VoxelShape SHAPE_WALL_SOUTH = Block.box(5D, 0D, 0D, 11D, 14D, 5D);
    protected static final VoxelShape SHAPE_WALL_WEST = Block.box(11D, 0D, 5D, 16D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_EAST = Block.box(0D, 0D, 5D, 5D, 14D, 11D);
    protected static final VoxelShape SHAPE_CEILING = Block.box(5D, 3D, 5D, 11D, 16D, 11D);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;

    private static final EnumMap<Direction, EnumMap<AttachFace, Int2ObjectMap<List<Vec3>>>> PARTICLE_OFFSETS;

    private static final List<Vec3> S2_FLOOR_1 = List.of(new Vec3(0.5, 0.6875 + 3 / 16f, 0.5));
    private static final List<Vec3> S2_FLOOR_3 = List.of(new Vec3(0.1875, 0.9375 - 1 / 16f, 0.5), new Vec3(0.5, 0.9375, 0.5), new Vec3(0.8125, 0.9375 - 1 / 16f, 0.5));
    private static final List<Vec3> S2_FLOOR_3f = List.of(new Vec3(0.5, 0.9375 - 1 / 16f, 0.1875), new Vec3(0.5, 0.9375, 0.5), new Vec3(0.5, 0.9375 - 1 / 16f, 0.8125));
    private final Supplier<Boolean> isFromSuppSquared = Suppliers.memoize(() ->
            !Utils.getID(this).getNamespace().equals(Supplementaries.MOD_ID));

    static {
        PARTICLE_OFFSETS = new EnumMap<>(Direction.class);
        EnumMap<AttachFace, Int2ObjectMap<List<Vec3>>> temp = new EnumMap<>(AttachFace.class);
        {
            Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectArrayMap<>();
            int2ObjectMap.put(1, List.of(new Vec3(0.5, 0.6875, 0.5)));
            int2ObjectMap.put(2, List.of(new Vec3(0.3125, 0.875, 0.5), new Vec3(0.6875, 0.875, 0.5)));
            int2ObjectMap.put(3, List.of(new Vec3(0.1875, 0.9375, 0.5), new Vec3(0.5, 0.9375, 0.5), new Vec3(0.8125, 0.9375, 0.5)));
            int2ObjectMap.put(4, List.of(new Vec3(0.1875, 1, 0.5), new Vec3(0.8125, 1, 0.5), new Vec3(0.5, 0.9375, 0.25), new Vec3(0.5, 0.9375, 0.75)));
            temp.put(AttachFace.FLOOR, Int2ObjectMaps.unmodifiable(int2ObjectMap));
        }
        {
            Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectArrayMap<>();
            int2ObjectMap.put(1, List.of(new Vec3(0.5, 0.9375, 0.1875)));
            int2ObjectMap.put(2, List.of(new Vec3(0.3125, 0.9375, 0.1875), new Vec3(0.6875, 0.9375, 0.1875)));
            int2ObjectMap.put(3, List.of(new Vec3(0.8125, 0.9375, 0.1875), new Vec3(0.1875, 0.9375, 0.1875), new Vec3(0.5, 0.9375, 0.25)));
            int2ObjectMap.put(4, List.of(new Vec3(0.1875, 1, 0.1875), new Vec3(0.8125, 1, 0.1875), new Vec3(0.3125, 0.875, 0.3125), new Vec3(0.6875, 0.875, 0.3125)));
            temp.put(AttachFace.WALL, Int2ObjectMaps.unmodifiable(int2ObjectMap));
        }
        {
            Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectArrayMap<>();
            int2ObjectMap.put(1, List.of(new Vec3(0.5, 9 / 16f, 0.5)));
            int2ObjectMap.put(2, List.of(new Vec3(0.25f, 0.875, 0.5), new Vec3(0.75, 0.875, 0.5)));
            int2ObjectMap.put(3, List.of(new Vec3(0.5f, 0.875, 0.75), new Vec3(0.75, 0.875, 0.375), new Vec3(0.25, 0.875, 0.375)));
            int2ObjectMap.put(4, List.of(new Vec3(0.1875, 0.8125, 0.1875), new Vec3(0.8125, 0.8125, 0.1875), new Vec3(0.8125, 0.8125, 0.8125), new Vec3(0.1875, 0.8125, 0.8125)));
            temp.put(AttachFace.CEILING, Int2ObjectMaps.unmodifiable(int2ObjectMap));
        }
        for (Direction direction : Direction.values()) {
            EnumMap<AttachFace, Int2ObjectMap<List<Vec3>>> newFaceMap = new EnumMap<>(AttachFace.class);
            for (var faceList : temp.entrySet()) {
                Int2ObjectMap<List<Vec3>> newCandleList = new Int2ObjectArrayMap<>();
                newCandleList.defaultReturnValue(List.of());
                int c = 1;
                var oldVec = faceList.getValue();
                for (int i = 1; i < 5; i++) {
                    ArrayList<Vec3> vectorsList = new ArrayList<>();
                    for (var vec : oldVec.get(i)) {
                        vectorsList.add(MthUtils.rotateVec3(vec.subtract(0.5, 0.5, 0.5), direction.getOpposite())
                                .add(0.5, 0.5, 0.5));
                    }
                    newCandleList.put(c++, ImmutableList.copyOf(vectorsList));
                }
                newFaceMap.put(faceList.getKey(), Int2ObjectMaps.unmodifiable(newCandleList));
            }
            PARTICLE_OFFSETS.put(direction, newFaceMap);
        }
    }

    @Nullable
    private final DyeColor color;
    private final Supplier<ParticleType<? extends ParticleOptions>> particle;

    public CandleHolderBlock(DyeColor color, Properties properties) {
        this(color, properties, () -> ParticleTypes.SMALL_FLAME);
    }

    public CandleHolderBlock(DyeColor color, Properties properties, Supplier<ParticleType<? extends ParticleOptions>> particle) {
        super(properties.lightLevel(CandleHolderBlock::lightLevel));
        this.color = color;
        this.particle = particle;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(LIT, false)
                .setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH).setValue(CANDLES, 1));
    }

    private static int lightLevel(BlockState state) {
        if (state.getValue(LIT)) {
            int candles = state.getValue(CANDLES);
            return 7 + candles * 2;
        }
        return 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.is(this)) {
            return blockState.setValue(CANDLES, Math.min(4, blockState.getValue(CANDLES) + 1));
        }
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, context.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate.setValue(WATERLOGGED, flag).setValue(LIT, false);
            }
        }
        return null;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return !useContext.isSecondaryUseActive() && useContext.getItemInHand().is(this.asItem()) && state.getValue(CANDLES) < 4 || super.canBeReplaced(state, useContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACE, FACING, CANDLES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> SHAPE_FLOOR;
            case WALL -> switch (state.getValue(FACING)) {
                default -> SHAPE_WALL_NORTH;
                case SOUTH -> SHAPE_WALL_SOUTH;
                case WEST -> SHAPE_WALL_WEST;
                case EAST -> SHAPE_WALL_EAST;
            };
            case CEILING -> SHAPE_CEILING;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context instanceof EntityCollisionContext ec && ec.getEntity() instanceof Projectile ? state.getShape(level, pos) : Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return canSupportCenter(worldIn, pos.below(), Direction.UP);
        } else if (state.getValue(FACE) == AttachFace.CEILING) {
            return IRopeConnection.isSupportingCeiling(pos.above(), worldIn);
        }
        return isSideSolidForDirection(worldIn, pos, state.getValue(FACING).getOpposite());
    }


    private void addParticlesAndSound(Level level, Vec3 offset, RandomSource random) {
        float f = random.nextFloat();
        if (f < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
            if (f < 0.17F) {
                level.playLocalSound(offset.x + 0.5, offset.y + 0.5, offset.z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
            }
        }
        level.addParticle((ParticleOptions) this.particle.get(), offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(LIT)) {
            getParticleOffset(state).forEach(v -> addParticlesAndSound(level, v.add(pos.getX(), pos.getY(), pos.getZ()), rand));
        }
    }

    private List<Vec3> getParticleOffset(BlockState state) {
        Direction direction = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);
        int candles = state.getValue(CANDLES);
        var v = PARTICLE_OFFSETS.get(direction).get(face).get(candles);
        if (isFromSuppSquared.get() && face == AttachFace.FLOOR) {
            if (candles == 1) return S2_FLOOR_1;
            if (candles == 3) return direction.getAxis() == Direction.Axis.Z ? S2_FLOOR_3 : S2_FLOOR_3f;
        }
        return v;
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
        return getFacing(stateIn).getOpposite() == facing && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }


    protected static Direction getFacing(BlockState state) {
        return switch (state.getValue(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> state.getValue(FACING);
        };
    }

    public static boolean isSideSolidForDirection(LevelReader reader, BlockPos pos, Direction direction) {
        BlockPos blockpos = pos.relative(direction);
        return reader.getBlockState(blockpos).isFaceSturdy(reader, blockpos, direction.getOpposite());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.General.TOOLTIP_HINTS.get() || !flagIn.isAdvanced()) return;
        tooltip.add((Component.translatable("message.supplementaries.candle_holder")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }

    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }

    @Override
    public boolean canBeExtinguishedBy(ItemStack item) {
        return item.isEmpty() || super.canBeExtinguishedBy(item);
    }

    @Override
    public void playExtinguishSound(LevelAccessor world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void spawnSmokeParticles(BlockState state, BlockPos pos, LevelAccessor level) {
        ((CandleHolderBlock) state.getBlock()).getParticleOffset(state).forEach(vec3 -> {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + vec3.x(), pos.getY() + vec3.y(), pos.getZ() + vec3.z(), 0.0, 0.10000000149011612, 0.0);
        });
    }
}
