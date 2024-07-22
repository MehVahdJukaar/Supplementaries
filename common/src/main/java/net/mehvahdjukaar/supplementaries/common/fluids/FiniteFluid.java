package net.mehvahdjukaar.supplementaries.common.fluids;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.function.Supplier;

public abstract class FiniteFluid extends Fluid {

    public static final IntegerProperty LEVEL = ModBlockProperties.FINITE_FLUID_LEVEL;
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    protected final int maxLayers;
    private final Supplier<? extends BucketItem> bucket;
    private final Supplier<? extends Block> block;

    public FiniteFluid(int maxLayers, Supplier<? extends Block> block, Supplier<? extends BucketItem> bucket) {
        this.maxLayers = maxLayers;
        this.block = block;
        this.bucket = bucket;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, maxLayers));
    }

    public int getLayersPerBlock() {
        return maxLayers;
    }

    @Override
    public Item getBucket() {
        return bucket.get();
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(LEVEL);
    }

    // flow direction. push force on entities
    @Override
    public Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
        return Vec3.ZERO;
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

            for (var e : map.entrySet()) {
                int oldAmount = e.getValue();
                Direction dir = e.getKey();
                BlockPos facingPos = pos.relative(dir);
                BlockState s = level.getBlockState(facingPos);
                FluidState fluidstate = makeState(oldAmount + 1);
                this.spreadTo(level, facingPos, s, dir, fluidstate);
            }


            if(!map.isEmpty()) {
                FluidState myNewState = makeState(currentAmount - map.size());
                BlockState blockstate = blockState.setValue(BlockStateProperties.LEVEL,
                        myNewState.createLegacyBlock().getValue(BlockStateProperties.LEVEL));
                level.setBlock(pos, blockstate, 2);
                level.updateNeighborsAt(pos, blockstate.getBlock());
            }
        }
    }

    public FluidState makeState(int level) {
        return this.defaultFluidState().setValue(LEVEL, level);
    }

    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction,
                            FluidState fluidState) {
        if (blockState.getBlock() instanceof LiquidBlockContainer container) {
            container.placeLiquid(level, pos, blockState, fluidState);
        } else {
            if (!blockState.isAir()) {
                this.beforeDestroyingBlock(level, pos, blockState);
            }
            level.setBlock(pos, fluidState.createLegacyBlock(), 3);
        }
    }

    protected Map<Direction, Integer> getWantedSpreadDirections(BlockGetter level, BlockPos pos, BlockState state) {
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
        if (state.isEmpty()) return;
        BlockState myState = level.getBlockState(pos);
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        // if it has this liquid below consolidate
        FluidState belowFluid = belowState.getFluidState();
        if (belowFluid.getType().isSame(this)) {
            if (belowFluid.getAmount() < maxLayers) {
                int belowMissing = maxLayers - belowFluid.getAmount();
                int belowAddition = Math.min(belowMissing, state.getAmount());
                int newAboveAmount = state.getAmount() - belowAddition;
                level.setBlockAndUpdate(belowPos, belowFluid.setValue(LEVEL, belowFluid.getAmount() + belowAddition)
                        .createLegacyBlock());
                if (newAboveAmount > 0) {
                    level.setBlockAndUpdate(pos, state.setValue(LEVEL, newAboveAmount).createLegacyBlock());
                } else level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            } else this.spreadToSides(level, pos, state, myState);

            return;
        }

        // if not
        if (this.canHoldFluid(level, belowPos, belowState)) {
            this.spreadTo(level, belowPos, belowState, Direction.DOWN, state);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else {
            this.spreadToSides(level, pos, state, myState);
        }
    }

    private boolean canHoldFluid(BlockGetter level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && !fluidState.is(this)) return false;
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer lc) {
            return lc.canPlaceLiquid(level, pos, state, this);
        } else if (!(block instanceof DoorBlock) && !state.is(BlockTags.SIGNS) && !state.is(Blocks.LADDER) &&
                !state.is(Blocks.SUGAR_CANE) && !state.is(Blocks.BUBBLE_COLUMN)) {
            if (!state.is(Blocks.NETHER_PORTAL) && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_GATEWAY) && !state.is(Blocks.STRUCTURE_VOID)) {
                return !state.blocksMotion();
            }
        }
        return false;
    }

    private static boolean hasSameAbove(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return fluidState.getType().isSame(level.getFluidState(pos.above()).getType());
    }

    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return hasSameAbove(state, level, pos) ? 1.0F : state.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return 14 / 16f * (float) state.getAmount() / (float) (maxLayers);
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


    public boolean shouldSlowDown(FluidState state) {
        return state.getAmount() > 2;
    }

    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 0;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return block.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    protected int getLegacyLevel(FluidState state) {
        int amount = state.getAmount();
        return maxLayers - Math.min(amount, maxLayers);
    }
}
