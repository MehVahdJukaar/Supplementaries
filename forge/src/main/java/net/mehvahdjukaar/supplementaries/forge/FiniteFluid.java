package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        } else if (state.isSource() || !this.isWaterHole(level, belowFluid.getType(), pos, myState, belowPos, belowState)) {
            this.spreadToSides(level, pos, state, myState);
        }
    }

    // where the magic happens
    private void spreadToSides(Level level, BlockPos pos, FluidState fluidState, BlockState blockState) {
        int currentAmount = fluidState.getAmount();

        if (currentAmount > 1) {
            List<Direction> list = this.getWantedSpreadDirections(level, pos, blockState);
            int bins = list.size() + 1;
            if (bins <= 1) return;

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

            int j = 1;
            FluidState myNewState = getFlowing(binCounts.get(0), false);
            int spreadDelay = this.getSpreadDelay(level, pos, myNewState, myNewState);
            BlockState blockstate = myNewState.createLegacyBlock();
            level.setBlock(pos, blockstate, 2);
            level.scheduleTick(pos, myNewState.getType(), spreadDelay);
            level.updateNeighborsAt(pos, blockstate.getBlock());


            for (var direction : list) {
                int newAmount = binCounts.get(j);
                j++;
                if(newAmount ==0)continue;//error
                BlockPos blockpos = pos.relative(direction);
                BlockState s = level.getBlockState(blockpos);
                if (this.canSpreadTo(level, pos, blockState, direction, blockpos, s, level.getFluidState(blockpos), this)) {
                    FluidState fluidstate = getFlowing(newAmount, false);
                    this.spreadTo(level, blockpos, s, direction, fluidstate);
                }
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

    protected int getSlopeDistance2(LevelReader level, BlockPos myPos, int recursionCounter, Direction fromDir, BlockState myState,
                                    BlockPos originPos, Short2ObjectMap<BlockState> blockStateCache, Short2BooleanMap holeCache) {
        int maxRecursion = 1000;

        for (Direction facingDir : Direction.Plane.HORIZONTAL) {
            if (fromDir != facingDir) {
                BlockPos blockpos = myPos.relative(facingDir);
                short short1 = getCacheKey(originPos, blockpos);
                BlockState blockstate = blockStateCache.computeIfAbsent(short1, (s) -> level.getBlockState(blockpos));
                FluidState fluidstate = blockstate.getFluidState();

                if (this.canPassThrough(level, this.getFlowing(), myPos, myState, facingDir, blockpos, blockstate, fluidstate)) {
                    boolean isWaterHole = holeCache.computeIfAbsent(short1, (s) -> {
                        BlockPos belowPos = blockpos.below();
                        BlockState belowState = level.getBlockState(belowPos);
                        return this.isWaterHole(level, this.getFlowing(), blockpos, blockstate, belowPos, belowState);
                    });
                    if (isWaterHole) {
                        return recursionCounter;
                    }

                    if (recursionCounter < this.getSlopeFindDistance(level)) {
                        int slopeDistance = this.getSlopeDistance2(level, blockpos, recursionCounter + 1,
                                fromDir.getOpposite(), blockstate, originPos, blockStateCache, holeCache);
                        if (slopeDistance < maxRecursion) {
                            maxRecursion = slopeDistance;
                        }
                    }
                }
            }
        }

        return maxRecursion;
    }

    private boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos arg3, BlockState arg4, BlockPos arg5, BlockState arg6) {
        if (!this.canPassThroughWall(Direction.DOWN, level, arg3, arg4, arg5, arg6)) {
            return false;
        } else {
            return arg6.getFluidState().getType().isSame(this) || this.canHoldFluid(level, arg5, arg6, fluid);
        }
    }

    private boolean canPassThrough(BlockGetter level, Fluid fluid, BlockPos arg3, BlockState arg4, Direction direction, BlockPos arg6, BlockState arg7, FluidState arg8) {
        return !this.isSourceBlockOfThisType(arg8) &&
                this.canPassThroughWall(direction, level, arg3, arg4, arg6, arg7) && this.canHoldFluid(level, arg6, arg7, fluid);
    }

    private boolean isSourceBlockOfThisType(FluidState state) {
        return state.getType().isSame(this);// && state.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader level);

    protected List<Direction> getWantedSpreadDirections(Level level, BlockPos pos, BlockState state) {
        int maxRecursion = 1000;
        List<Direction> list = new ArrayList<>();
        Short2ObjectMap<BlockState> neighborBlocks = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = pos.relative(direction);
            short key = getCacheKey(pos, facingPos);

            BlockState facingState = neighborBlocks.computeIfAbsent(key, (s) -> level.getBlockState(facingPos));
            FluidState facingFluid = facingState.getFluidState();

            //change
            if (this.canPassThrough(level, this, pos, state, direction, facingPos, facingState, facingFluid)) {
                BlockPos belowFacingPos = facingPos.below();
                boolean hasHoleBelow = short2booleanmap.computeIfAbsent(key, (s) -> {
                    BlockState belowFacingState = level.getBlockState(belowFacingPos);
                    return this.isWaterHole(level, this.getFlowing(), facingPos, facingState, belowFacingPos, belowFacingState);
                });
                int holeDistance;
                if (hasHoleBelow) {
                    holeDistance = 0;
                } else {
                    holeDistance = this.getSlopeDistance2(level, facingPos, 1, direction.getOpposite(), facingState, pos,
                            neighborBlocks, short2booleanmap);
                }

                if (holeDistance < maxRecursion) {
                    list.clear();
                }

                if (holeDistance <= maxRecursion) {
                    list.add(direction);
                    maxRecursion = holeDistance;
                }
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

    protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
        return toFluidState.canBeReplacedWith(level, toPos, fluid, direction) && this.canPassThroughWall(direction, level, fromPos, fromBlockState, toPos, toBlockState) && this.canHoldFluid(level, toPos, toBlockState, fluid);
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
