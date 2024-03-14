package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.mehvahdjukaar.supplementaries.forge.FiniteLiquidBlock.MAX_LEVEL;

// copy paste from flowing fluid. once main logic is determined, redundant overrides can be removed
public abstract class FiniteFluid extends FlowingFluid {
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = ModBlockProperties.FINITE_FLUID_LEVEL;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE;
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING, LEVEL);
    }

    // flow direction. push force on entities
    @Override
    public Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
        return Vec3.ZERO;
    }

    private boolean affectsFlow(FluidState state) {
        return state.isEmpty() || state.getType().isSame(this);
    }

    @Override
    protected boolean isSolidFace(BlockGetter level, BlockPos neighborPos, Direction side) {
        BlockState blockstate = level.getBlockState(neighborPos);
        FluidState fluidstate = level.getFluidState(neighborPos);
        if (fluidstate.getType().isSame(this)) {
            return false;
        } else if (side == Direction.UP) {
            return true;
        } else {
            return blockstate.getBlock() instanceof IceBlock ? false : blockstate.isFaceSturdy(level, neighborPos, side);
        }
    }

    @Override
    protected void spread(Level level, BlockPos pos, FluidState state) {
        if (state.isEmpty()) return;
        BlockState myState = level.getBlockState(pos);
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (this.canSpreadTo(level, pos, myState, Direction.DOWN, belowPos, belowState,
                level.getFluidState(belowPos), this)) {
            this.spreadTo(level, belowPos, belowState, Direction.DOWN, state);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else if (!this.isWaterHole(level, this, pos, myState, belowPos, belowState)) {
            this.spreadToSides(level, pos, state, myState);
        }
    }

    private boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos arg3, BlockState arg4, BlockPos arg5, BlockState arg6) {
        if (!this.canPassThroughWall(Direction.DOWN, level, arg3, arg4, arg5, arg6)) {
            return false;
        } else {
            return arg6.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(level, arg5, arg6, fluid);
        }
    }

    // where the magic happens
    private void spreadToSides(Level level, BlockPos pos, FluidState fluidState, BlockState blockState) {
        int currentAmount = fluidState.getAmount();

        Map<Direction, Integer> map = this.getWantedSpreadDirections(level, pos, blockState);

        int nonZero = 1;
        int extra = 0;
        for (var e : map.values()) {
            extra += e;
            if (e != 0) nonZero += 1;
        }
        currentAmount += extra;
        float average = currentAmount / ((float) nonZero);
        if (currentAmount > 1 && average > 1) {

            List<Direction> dirList = map.keySet().stream().toList();//.stream().filter(e -> e.getValue() == 0).map(Map.Entry::getKey).toList();

            int bins = dirList.size();
            if (bins < 1) {
                return;
            }

            currentAmount -= 1;
            List<Integer> binCounts = new ArrayList<>();

            int ballsPerBin = currentAmount / bins;
            int remainder = currentAmount % bins;

            for (int i = 0; i < bins; i++) {
                if (i < remainder) {
                    binCounts.add(ballsPerBin + 1);
                } else {
                    binCounts.add(ballsPerBin);
                }
            }

            FluidState myNewState = getFlowing(1, false);
            BlockState blockstate = myNewState.createLegacyBlock();
            level.setBlock(pos, blockstate, 2);

            level.updateNeighborsAt(pos, blockstate.getBlock());
            int j = 0;

            for (var direction : dirList) {
                int newAmount = binCounts.get(j);
                j++;
                if (newAmount == 0) continue;//error
                BlockPos blockpos = pos.relative(direction);
                BlockState s = level.getBlockState(blockpos);
                //if (this.canSpreadTo(level, pos, blockState, direction, blockpos, s, level.getFluidState(blockpos), this)) {
                FluidState fluidstate = getFlowing(newAmount, false);
                this.spreadTo(level, blockpos, s, direction, fluidstate);
                //}
            }
        }

    }

    // dummy overrides

    // gets new fluid state for this position (supposedly it was already air). Used for flowing fluid expansion which we dont have
    @Override
    protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    protected Map<Direction, FluidState> getSpread(Level level, BlockPos pos, BlockState state) {
        return Map.of();
    }

    @Override
    protected int getSlopeDistance(LevelReader level, BlockPos arg2, int k, Direction direction, BlockState arg4, BlockPos arg5, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        return 1000;
    }


    public abstract Fluid getFlowing();

    @Override
    public FluidState getFlowing(int level, boolean falling) {
        return (this.getFlowing().defaultFluidState().setValue(LEVEL, level)).setValue(FALLING, falling);
    }

    @Override
    public FluidState getSource(boolean falling) {
        return this.getSource().defaultFluidState()
                .setValue(LEVEL, MAX_LEVEL).setValue(FALLING, falling);
    }

    @Override
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (blockState.getBlock() instanceof LiquidBlockContainer container) {
            container.placeLiquid(level, pos, blockState, fluidState);
        } else {
            if (!blockState.isAir()) {
                this.beforeDestroyingBlock(level, pos, blockState);
            }
            level.setBlock(pos, fluidState.createLegacyBlock(), 3);
        }
    }

    @Override
    protected abstract void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state);

    private static short getCacheKey(BlockPos arg, BlockPos arg2) {
        int i = arg2.getX() - arg.getX();
        int j = arg2.getZ() - arg.getZ();
        return (short) ((i + 128 & 255) << 8 | j + 128 & 255);
    }

    private boolean isSourceBlockOfThisType(FluidState state) {
        return state.getType().isSame(this);// && state.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader level);

    protected Map<Direction, Integer> getWantedSpreadDirections(Level level, BlockPos pos, BlockState state) {
        Map<Direction, Integer> list = new HashMap<>();
        Short2ObjectMap<BlockState> neighborBlocks = new Short2ObjectOpenHashMap<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = pos.relative(direction);
            short key = getCacheKey(pos, facingPos);

            BlockState facingState = neighborBlocks.computeIfAbsent(key, (s) -> level.getBlockState(facingPos));
            FluidState facingFluid = facingState.getFluidState();

            if (this.canHoldFluid(level, facingPos, facingState, this)) {
                list.put(direction, facingFluid.getAmount());
            }
        }

        return list;
    }

    private boolean canHoldFluid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer) block).canPlaceLiquid(level, pos, state, fluid);
        } else if (!(block instanceof DoorBlock) && !state.is(BlockTags.SIGNS) && !state.is(Blocks.LADDER) && !state.is(Blocks.SUGAR_CANE) && !state.is(Blocks.BUBBLE_COLUMN)) {
            if (!state.is(Blocks.NETHER_PORTAL) && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_GATEWAY) && !state.is(Blocks.STRUCTURE_VOID)) {
                return !state.blocksMotion();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //remove
    protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
        return toFluidState.canBeReplacedWith(level, toPos, fluid, direction) && this.canPassThroughWall(direction, level, fromPos, fromBlockState, toPos, toBlockState) && this.canHoldFluid(level, toPos, toBlockState, fluid);
    }

    private boolean canPassThroughWall(Direction direction, BlockGetter level, BlockPos arg3, BlockState arg4, BlockPos arg5, BlockState arg6) {
        Object2ByteLinkedOpenHashMap object2bytelinkedopenhashmap;
        if (!arg4.getBlock().hasDynamicShape() && !arg6.getBlock().hasDynamicShape()) {
            object2bytelinkedopenhashmap = (Object2ByteLinkedOpenHashMap) OCCLUSION_CACHE.get();
        } else {
            object2bytelinkedopenhashmap = null;
        }

        Block.BlockStatePairKey block$blockstatepairkey;
        if (object2bytelinkedopenhashmap != null) {
            block$blockstatepairkey = new Block.BlockStatePairKey(arg4, arg6, direction);
            byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
            if (b0 != 127) {
                return b0 != 0;
            }
        } else {
            block$blockstatepairkey = null;
        }

        VoxelShape voxelshape1 = arg4.getCollisionShape(level, arg3);
        VoxelShape voxelshape = arg6.getCollisionShape(level, arg5);
        boolean flag = !Shapes.mergedFaceOccludes(voxelshape1, voxelshape, direction);
        if (object2bytelinkedopenhashmap != null) {
            if (object2bytelinkedopenhashmap.size() == 200) {
                object2bytelinkedopenhashmap.removeLastByte();
            }

            object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte) (flag ? 1 : 0));
        }

        return flag;
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        this.spread(level, pos, state);
    }

    public abstract int getSpreadDelay(Level level, BlockPos pos, FluidState state, FluidState fluidstate);

    private static boolean hasSameAbove(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return fluidState.getType().isSame(level.getFluidState(pos.above()).getType());
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return hasSameAbove(state, level, pos) ? 1.0F : state.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return (float) state.getAmount() / 14.0F;
    }

    @Override
    public abstract int getAmount(FluidState state);

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return state.getAmount() == 14 && hasSameAbove(state, level, pos) ? Shapes.block() :
                this.shapes.computeIfAbsent(state, (arg3) -> Shapes.box(0.0, 0.0, 0.0, 1.0,
                        arg3.getHeight(level, pos), 1.0));
    }

    static {
        OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> map = new Object2ByteLinkedOpenHashMap<>(CACHE_SIZE) {
                protected void rehash(int i) {
                }
            };
            map.defaultReturnValue((byte) 127);
            return map;
        });
    }
}
