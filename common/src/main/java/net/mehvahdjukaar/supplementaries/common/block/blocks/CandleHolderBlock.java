package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CandleHolderBlock extends LightUpWaterBlock {
    protected static final VoxelShape SHAPE_FLOOR = Block.box(5D, 0D, 5D, 11D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_NORTH = Block.box(5D, 0D, 11D, 11D, 14D, 16D);
    protected static final VoxelShape SHAPE_WALL_SOUTH = Block.box(5D, 0D, 0D, 11D, 14D, 5D);
    protected static final VoxelShape SHAPE_WALL_WEST = Block.box(11D, 0D, 5D, 16D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_EAST = Block.box(0D, 0D, 5D, 5D, 14D, 11D);
    protected static final VoxelShape SHAPE_CEILING = Block.box(5D, 3D, 5D, 11D, 16D, 11D);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;

    private static EnumMap<Direction, EnumMap<AttachFace, Int2ObjectMap<List<Vec3>>>> PARTICLE_OFFSETS;

    static {
        createParticleList();
    }

    private static void createParticleList() {
        PARTICLE_OFFSETS = new EnumMap<>(Direction.class);
        EnumMap<AttachFace, Int2ObjectMap<List<Vec3>>> temp = new EnumMap<>(AttachFace.class);
        {
            Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectOpenHashMap<>();
            int2ObjectMap.put(1, List.of(new Vec3(0.5, 0.6875, 0.5)));
            int2ObjectMap.put(2, List.of(new Vec3(0.3125, 0.875, 0.5), new Vec3(0.6875, 0.875, 0.5)));
            int2ObjectMap.put(3, List.of(new Vec3(0.1875, 0.9375, 0.5), new Vec3(0.5, 0.9375, 0.5), new Vec3(0.8125, 0.9375, 0.5)));
            int2ObjectMap.put(4, List.of(new Vec3(0.1875, 1, 0.5), new Vec3(0.8125, 1, 0.5), new Vec3(0.5, 0.9375, 0.25), new Vec3(0.5, 0.9375, 0.75)));
            temp.put(AttachFace.FLOOR, Int2ObjectMaps.unmodifiable(int2ObjectMap));
        }
        {
            Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectOpenHashMap<>();
            int2ObjectMap.put(1, List.of(new Vec3(0.5, 0.9375, 0.1875)));
            int2ObjectMap.put(2, List.of(new Vec3(0.3125, 0.9375, 0.1875), new Vec3(0.6875, 0.9375, 0.1875)));
            int2ObjectMap.put(3, List.of(new Vec3(0.8125, 0.9375, 0.1875), new Vec3(0.1875, 0.9375, 0.1875), new Vec3(0.5, 0.9375, 0.25)));
            int2ObjectMap.put(4, List.of(new Vec3(0.1875, 1, 0.1875), new Vec3(0.8125, 1, 0.1875), new Vec3(0.3125, 0.875, 0.3125), new Vec3(0.6875, 0.875, 0.3125)));
            temp.put(AttachFace.WALL, Int2ObjectMaps.unmodifiable(int2ObjectMap));
        }
        {
            Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectOpenHashMap<>();
            int2ObjectMap.put(1, List.of(new Vec3(0.5, 0.5, 0.5)));
            int2ObjectMap.put(2, List.of(new Vec3(0.375, 0.44, 0.5), new Vec3(0.625, 0.5, 0.44)));
            int2ObjectMap.put(3, List.of(new Vec3(0.5, 0.313, 0.625), new Vec3(0.375, 0.44, 0.5), new Vec3(0.56, 0.5, 0.44)));
            int2ObjectMap.put(4, List.of(new Vec3(0.44, 0.313, 0.56), new Vec3(0.625, 0.44, 0.56), new Vec3(0.375, 0.44, 0.375), new Vec3(0.56, 0.5, 0.375)));
            temp.put(AttachFace.CEILING, Int2ObjectMaps.unmodifiable(int2ObjectMap));
        }
        for (Direction direction : Direction.values()) {
            EnumMap<AttachFace, Int2ObjectMap<List<Vec3>>> newFaceMap = new EnumMap<>(AttachFace.class);
            for (var faceList : temp.entrySet()) {
                Int2ObjectMap<List<Vec3>> newCandleList = new Int2ObjectOpenHashMap<>();
                newCandleList.defaultReturnValue(ImmutableList.of());
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

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        createParticleList();
        return super.use(state, worldIn, pos, player, handIn, hit);
    }

    public CandleHolderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(LIT, true)
                .setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH).setValue(CANDLES, 1));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, context.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate.setValue(WATERLOGGED, flag).setValue(LIT, !flag);
            }
        }
        return null;
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
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return canSupportCenter(worldIn, pos.below(), Direction.UP);
        } else if (state.getValue(FACE) == AttachFace.CEILING) {
            return RopeBlock.isSupportingCeiling(pos.above(), worldIn);
        }
        return isSideSolidForDirection(worldIn, pos, state.getValue(FACING).getOpposite());
    }


    private static void addParticlesAndSound(Level level, Vec3 offset, RandomSource random) {
        float f = random.nextFloat();
        if (f < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
            if (f < 0.17F) {
                //    level.playLocalSound(offset.x + 0.5, offset.y + 0.5, offset.z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
            }
        }
        level.addParticle(ParticleTypes.SMALL_FLAME, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (!state.getValue(LIT)) return;
        Direction direction = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);

        PARTICLE_OFFSETS.get(direction).get(face).get((int) state.getValue(CANDLES))
                .forEach(v -> addParticlesAndSound(level, v.add(pos.getX(), pos.getY(), pos.getZ()), rand));
        if (true) return;
        double xm, ym, zm, xl, yl, zl, xr, zr;
        Direction dir = direction.getClockWise();
        double xOff = dir.getStepX() * 0.3125D;
        double zOff = dir.getStepZ() * 0.3125D;
        switch (state.getValue(FACE)) {
            default -> {
                xm = pos.getX() + 0.5D;
                ym = pos.getY() + 1D;
                zm = pos.getZ() + 0.5D;
                xl = pos.getX() + 0.5D - xOff;
                yl = pos.getY() + 0.9375D;
                zl = pos.getZ() + 0.5D - zOff;
                xr = pos.getX() + 0.5D + xOff;
                zr = pos.getZ() + 0.5D + zOff;
            }
            case WALL -> {
                double xo1 = -direction.getStepX() * 0.3125;
                double zo2 = -direction.getStepZ() * 0.3125;
                xm = pos.getX() + 0.5D + xo1;
                ym = pos.getY() + 1;
                zm = pos.getZ() + 0.5D + zo2;
                xl = pos.getX() + 0.5D + xo1 - xOff;
                yl = pos.getY() + 0.9375;
                zl = pos.getZ() + 0.5D + zo2 - zOff;
                xr = pos.getX() + 0.5D + xo1 + xOff;
                zr = pos.getZ() + 0.5D + zo2 + zOff;
            }
            case CEILING -> {
                //high
                xm = pos.getX() + 0.5D + zOff;
                zm = pos.getZ() + 0.5D - xOff;
                ym = pos.getY() + 0.875;//0.9375D;

                //2 medium
                xl = pos.getX() + 0.5D + xOff;
                zl = pos.getZ() + 0.5D + zOff;
                xr = pos.getX() + 0.5D - zOff;
                zr = pos.getZ() + 0.5D + xOff;
                yl = pos.getY() + 0.8125;
                double xs = pos.getX() + 0.5D - xOff;
                double zs = pos.getZ() + 0.5D - zOff;
                double ys = pos.getY() + 0.75;
                level.addParticle(ParticleTypes.FLAME, xs, ys, zs, 0, 0, 0);
            }
        }
        level.addParticle(ParticleTypes.FLAME, xm, ym, zm, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, xl, yl, zl, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, xr, yl, zr, 0, 0, 0);

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
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
