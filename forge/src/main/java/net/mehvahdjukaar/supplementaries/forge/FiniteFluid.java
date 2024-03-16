package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

// copy paste from flowing fluid. once main logic is determined, redundant overrides can be removed
public abstract class FiniteFluid extends Fluid {
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

    private boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos arg3, BlockState arg4, BlockPos arg5, BlockState state) {
        return state.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(level, arg5, state);
    }
    //remove
    protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
        return this.canHoldFluid(level, toPos, toBlockState);
    }
    private boolean canHoldFluid(BlockGetter level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() &&  !fluidState.is(this)) return false;
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer) block).canPlaceLiquid(level, pos, state, this);
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

    // where the magic happens
    private void spreadToSides(Level level, BlockPos pos, FluidState fluidState, BlockState blockState) {
        int currentAmount = fluidState.getAmount();

        //if(currentAmount<10)return;
        if (currentAmount > 1) {
            Map<Direction, Integer> map = this.getWantedSpreadDirections(level, pos, blockState);
            //remove entries greater than amount. copilot pls
            // Convert LinkedHashMap to a list of entries
            List<Map.Entry<Direction, Integer>> entryList = new ArrayList<>(map.entrySet());
            // Shuffle the list
            Collections.shuffle(entryList);
            map = new LinkedHashMap<>();
            for (var e : entryList) {
                map.put(e.getKey(), e.getValue());
            }

            int initialAmount = currentAmount;
            map.values().removeIf(i -> i >= initialAmount);


            int r = 1;
            while (currentAmount <= map.size()) {
                int finalR = r;
                r++;
                var iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    var e = iter.next();
                    if (e.getValue() > initialAmount - finalR) {
                        iter.remove();
                    }
                    if (currentAmount > map.size()) {
                        break;
                    }
                }
            }

            // only considers lower fill level


            int j = 0;

            for (var e : map.entrySet()) {
                int oldAmount = e.getValue();
                Direction dir = e.getKey();
                BlockPos facingPos = pos.relative(dir);
                BlockState s = level.getBlockState(facingPos);
                FluidState fluidstate = makeState(oldAmount + 1, false);
                this.spreadTo(level, facingPos, s, dir, fluidstate);
            }


            FluidState myNewState = makeState(currentAmount - map.size(), false);
            BlockState blockstate = myNewState.createLegacyBlock();
            level.setBlock(pos, blockstate, 2);
            level.updateNeighborsAt(pos, blockstate.getBlock());
        }
    }

    public FluidState makeState(int level, boolean falling) {
        return (this.defaultFluidState().setValue(LEVEL, level)).setValue(FALLING, falling);
    }

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

    protected abstract void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state);


    protected Map<Direction, Integer> getWantedSpreadDirections(Level level, BlockPos pos, BlockState state) {
        Map<Direction, Integer> list = new HashMap<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = pos.relative(direction);

            BlockState facingState = level.getBlockState(facingPos);
            FluidState facingFluid = facingState.getFluidState();

            if (this.canHoldFluid(level, facingPos, facingState)) {
                list.put(direction, facingFluid.getAmount());
            }
        }

        return list;
    }



    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        this.spread(level, pos, state);
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return state.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return (float) state.getAmount() / 14.0F;
    }

    // needed for bucket pickup raytrace
    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return this.shapes.computeIfAbsent(state, (arg3) -> Shapes.box(0.0, 0.0, 0.0, 1.0,
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

    public boolean shouldSlowDown(FluidState state) {
        return state.getAmount() > 2;
    }
}
