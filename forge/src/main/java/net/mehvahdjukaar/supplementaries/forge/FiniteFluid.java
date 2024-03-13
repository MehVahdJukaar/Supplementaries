package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
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

import java.util.Iterator;
import java.util.Map;

// copy paste from flowing fluid. once main logic is determined, redundant overrides can be removed
public abstract class FiniteFluid extends FlowingFluid {
    public static final BooleanProperty FALLING;
    public static final IntegerProperty LEVEL;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE;
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    // flow direction. push force on entities
    @Override
    public Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
        double d0 = 0.0;
        double d1 = 0.0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blockpos$mutableblockpos.setWithOffset(pos, (Direction) direction);
            FluidState fluidstate = blockReader.getFluidState(blockpos$mutableblockpos);
            if (this.affectsFlow(fluidstate)) {
                float f = fluidstate.getOwnHeight();
                float f1 = 0.0F;
                if (f == 0.0F) {
                    if (!blockReader.getBlockState(blockpos$mutableblockpos).blocksMotion()) {
                        BlockPos blockpos = blockpos$mutableblockpos.below();
                        FluidState fluidstate1 = blockReader.getFluidState(blockpos);
                        if (this.affectsFlow(fluidstate1)) {
                            f = fluidstate1.getOwnHeight();
                            if (f > 0.0F) {
                                f1 = fluidState.getOwnHeight() - (f - 0.8888889F);
                            }
                        }
                    }
                } else if (f > 0.0F) {
                    f1 = fluidState.getOwnHeight() - f;
                }

                if (f1 != 0.0F) {
                    d0 += ((float) direction.getStepX() * f1);
                    d1 += ((float) direction.getStepZ() * f1);
                }
            }
        }

        Vec3 vec3 = new Vec3(d0, 0.0, d1);
        if (fluidState.getValue(FALLING)) {
            Iterator<Direction> var17 = Direction.Plane.HORIZONTAL.iterator();

            Direction direction1;
            do {
                if (!var17.hasNext()) {
                    return vec3.normalize();
                }

                direction1 = var17.next();
                blockpos$mutableblockpos.setWithOffset(pos, direction1);
            } while (!this.isSolidFace(blockReader, blockpos$mutableblockpos, direction1) && !this.isSolidFace(blockReader, blockpos$mutableblockpos.above(), direction1));

            vec3 = vec3.normalize().add(0.0, -6.0, 0.0);
        }

        return vec3.normalize();
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
        FluidState belowFluid = this.getNewLiquid(level, belowPos, belowState);
        if (this.canSpreadTo(level, pos, myState, Direction.DOWN, belowPos, belowState,
                level.getFluidState(belowPos), belowFluid.getType())) {
            this.spreadTo(level, belowPos, belowState, Direction.DOWN, belowFluid);
            if (this.sourceNeighborCount(level, pos) >= 3) {
                this.spreadToSides(level, pos, state, myState);
            }
        } else if (state.isSource() || !this.isWaterHole(level, belowFluid.getType(), pos, myState, belowPos, belowState)) {
            this.spreadToSides(level, pos, state, myState);
        }
    }

    // where the magic happens
    private void spreadToSides(Level level, BlockPos pos, FluidState fluidState, BlockState blockState) {
        int newAmount = fluidState.getAmount() - this.getDropOff(level);
        if (fluidState.getValue(FALLING)) {
            newAmount = 7;
        }

        if (newAmount > 0) {
            Map<Direction, FluidState> map = this.getSpread(level, pos, blockState);

            for (Map.Entry<Direction, FluidState> directionFluidStateEntry : map.entrySet()) {
                Direction direction = directionFluidStateEntry.getKey();
                FluidState fluidstate = directionFluidStateEntry.getValue();
                BlockPos blockpos = pos.relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);
                if (this.canSpreadTo(level, pos, blockState, direction, blockpos, blockstate, level.getFluidState(blockpos), fluidstate.getType())) {
                    this.spreadTo(level, blockpos, blockstate, direction, fluidstate);
                }
            }
        }

    }

    // gets new fluid state for this position
    @Override
    protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState) {
        int i = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = pos.relative(direction);
            BlockState facingState = level.getBlockState(facingPos);
            FluidState facingFluid = facingState.getFluidState();
            if (facingFluid.getType().isSame(this) && this.canPassThroughWall(direction, level, pos, blockState, facingPos, facingState)) {
                i = Math.max(i, facingFluid.getAmount());
            }
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        FluidState aboveFluid = aboveState.getFluidState();
        if (!aboveFluid.isEmpty() && aboveFluid.getType().isSame(this) && this.canPassThroughWall(Direction.UP, level, pos, blockState, abovePos, aboveState)) {
            //turn to source when aoove is source
            return this.getFlowing(8, true);
        } else {
            int k = i - this.getDropOff(level);
            return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    //no clue
    private boolean canPassThroughWall(Direction direction, BlockGetter level, BlockPos arg3, BlockState arg4, BlockPos arg5, BlockState arg6) {
        Object2ByteLinkedOpenHashMap object2bytelinkedopenhashmap;
        if (!arg4.getBlock().hasDynamicShape() && !arg6.getBlock().hasDynamicShape()) {
            object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
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


    public abstract Fluid getFlowing();

    public FluidState getFlowing(int level, boolean falling) {
        return (this.getFlowing().defaultFluidState().setValue(LEVEL, level)).setValue(FALLING, falling);
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

    protected int getSlopeDistance2(LevelReader level, BlockPos arg2, int k, Direction direction, BlockState arg4,
                                    BlockPos arg5, Short2ObjectMap<BlockState> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        int i = 1000;

        for (Direction facingDir : Direction.Plane.HORIZONTAL) {
            if (direction != facingDir) {
                BlockPos blockpos = arg2.relative(facingDir);
                short short1 = getCacheKey(arg5, blockpos);
                BlockState blockstate = short2ObjectMap.computeIfAbsent(short1, (s) -> level.getBlockState(blockpos));
                FluidState fluidstate = blockstate.getFluidState();

                if (this.canPassThrough(level, this.getFlowing(), arg2, arg4, facingDir, blockpos, blockstate, fluidstate)) {
                    boolean flag = short2BooleanMap.computeIfAbsent(short1, (s) -> {
                        BlockPos blockpos1 = blockpos.below();
                        BlockState blockstate1 = level.getBlockState(blockpos1);
                        return this.isWaterHole(level, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                    });
                    if (flag) {
                        return k;
                    }

                    if (k < this.getSlopeFindDistance(level)) {
                        int j = this.getSlopeDistance2(level, blockpos, k + 1, direction.getOpposite(), blockstate, arg5, short2ObjectMap, short2BooleanMap);
                        if (j < i) {
                            i = j;
                        }
                    }
                }
            }
        }

        return i;
    }

    private boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos arg3, BlockState arg4, BlockPos arg5, BlockState arg6) {
        if (!this.canPassThroughWall(Direction.DOWN, level, arg3, arg4, arg5, arg6)) {
            return false;
        } else {
            return arg6.getFluidState().getType().isSame(this) || this.canHoldFluid(level, arg5, arg6, fluid);
        }
    }

    private boolean canPassThrough(BlockGetter level, Fluid fluid, BlockPos arg3, BlockState arg4, Direction direction, BlockPos arg6, BlockState arg7, FluidState arg8) {
        return !this.isSourceBlockOfThisType(arg8) && this.canPassThroughWall(direction, level, arg3, arg4, arg6, arg7) && this.canHoldFluid(level, arg6, arg7, fluid);
    }

    private boolean isSourceBlockOfThisType(FluidState state) {
        return state.getType().isSame(this) && state.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader level);

    /**
     * Returns the number of immediately adjacent source blocks of the same fluid that lie on the horizontal plane.
     */
    private int sourceNeighborCount(LevelReader level, BlockPos pos) {
        int i = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            FluidState fluidstate = level.getFluidState(blockpos);
            if (this.isSourceBlockOfThisType(fluidstate)) {
                ++i;
            }
        }

        return i;
    }

    protected Map<Direction, FluidState> getSpread(Level level, BlockPos pos, BlockState state) {
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<BlockState> neighborBlocks = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = pos.relative(direction);
            short key = getCacheKey(pos, facingPos);

            BlockState facingState = neighborBlocks.computeIfAbsent(key, (s) -> level.getBlockState(facingPos));
            FluidState facingFluid = facingState.getFluidState();

            FluidState newFacingFluid = this.getNewLiquid(level, facingPos, facingState);
            if (this.canPassThrough(level, newFacingFluid.getType(), pos, state, direction, facingPos, facingState, facingFluid)) {
                BlockPos belowFacingPos = facingPos.below();
                boolean flag = short2booleanmap.computeIfAbsent(key, (s) -> {
                    BlockState belowFacingState = level.getBlockState(belowFacingPos);
                    return this.isWaterHole(level, this.getFlowing(), facingPos, facingState, belowFacingPos, belowFacingState);
                });
                int j;
                if (flag) {
                    j = 0;
                } else {
                    j = this.getSlopeDistance2(level, facingPos, 1, direction.getOpposite(), facingState, pos,
                            neighborBlocks, short2booleanmap);
                }

                if (j < i) {
                    map.clear();
                }

                if (j <= i) {
                    map.put(direction, newFacingFluid);
                    i = j;
                }
            }
        }

        return map;
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

    protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
        return toFluidState.canBeReplacedWith(level, toPos, fluid, direction) && this.canPassThroughWall(direction, level, fromPos, fromBlockState, toPos, toBlockState) && this.canHoldFluid(level, toPos, toBlockState, fluid);
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        if (!state.isSource()) {
            FluidState fluidstate = this.getNewLiquid(level, pos, level.getBlockState(pos));
            int spreadDelay = this.getSpreadDelay(level, pos, state, fluidstate);
            if (fluidstate.isEmpty()) {
                state = fluidstate;
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else if (!fluidstate.equals(state)) {
                state = fluidstate;
                BlockState blockstate = fluidstate.createLegacyBlock();
                level.setBlock(pos, blockstate, 2);
                level.scheduleTick(pos, fluidstate.getType(), spreadDelay);
                level.updateNeighborsAt(pos, blockstate.getBlock());
            }
        }

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
        return (float) state.getAmount() / 9.0F;
    }

    @Override
    public abstract int getAmount(FluidState state);

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return state.getAmount() == 9 && hasSameAbove(state, level, pos) ? Shapes.block() :
                this.shapes.computeIfAbsent(state, (arg3) -> Shapes.box(0.0, 0.0, 0.0, 1.0,
                        arg3.getHeight(level, pos), 1.0));
    }

    static {
        FALLING = BlockStateProperties.FALLING;
        LEVEL = BlockStateProperties.LEVEL_FLOWING;
        OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) {
                protected void rehash(int i) {
                }
            };
            object2bytelinkedopenhashmap.defaultReturnValue((byte) 127);
            return object2bytelinkedopenhashmap;
        });
    }
}
