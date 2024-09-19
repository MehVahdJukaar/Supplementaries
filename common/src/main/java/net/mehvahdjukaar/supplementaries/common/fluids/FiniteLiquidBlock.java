package net.mehvahdjukaar.supplementaries.common.fluids;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

// diff property means we need a diff class
public class FiniteLiquidBlock extends Block implements BucketPickup, LiquidBlockContainer {

    public static final VoxelShape STABLE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final IntegerProperty MISSING_LEVELS = BlockStateProperties.LEVEL;

    private final List<FluidState> stateCache;
    private final FiniteFluid fluid;
    public final int maxLevel;
    private boolean fluidStateCacheInitialized = false;


    public FiniteLiquidBlock(Supplier<? extends FiniteFluid> supplier, BlockBehaviour.Properties arg) {
        super(arg);
        this.fluid = supplier.get();
        this.maxLevel = fluid.maxLayers;
        assert maxLevel <= 16;
        this.stateCache = Lists.newArrayList();
        this.registerDefaultState((this.stateDefinition.any()).setValue(MISSING_LEVELS, 0));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        int i = state.getValue(MISSING_LEVELS);
        if (!this.fluidStateCacheInitialized) {
            this.initFluidStateCache();
        }

        return this.stateCache.get(Math.min(i, maxLevel));
    }

    protected synchronized void initFluidStateCache() {
        if (!this.fluidStateCacheInitialized) {
            this.stateCache.add(this.fluid.makeState(maxLevel));

            for (int i = 1; i < maxLevel; ++i) {
                this.stateCache.add(this.fluid.makeState(maxLevel - i));
            }
            this.fluidStateCacheInitialized = true;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isAbove(STABLE_SHAPE, pos, true) && state.getValue(MISSING_LEVELS) == 0 && context.canStandOnFluid(level.getFluidState(pos.above()), state.getFluidState()) ? STABLE_SHAPE : Shapes.empty();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getFluidState().isRandomlyTicking();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        state.getFluidState().randomTick(level, pos, random);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction direction) {
        return adjacentBlockState.getFluidState().getType().isSame(this.fluid);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState arg, LootParams.Builder arg2) {
        return Collections.emptyList();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        level.scheduleTick(pos, state.getFluidState().getType(), this.fluid.getTickDelay(level));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getFluidState().isSource() || neighborState.getFluidState().isSource()) {
            level.scheduleTick(currentPos, state.getFluidState().getType(), this.fluid.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        level.scheduleTick(pos, state.getFluidState().getType(), this.fluid.getTickDelay(level));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MISSING_LEVELS);
    }

    @Override
    public ItemStack pickupBlock(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        //find connected blocks around. if their LEVEL sum is greater than 13 pickup fluid and delete them
        AtomicInteger currentLevel = new AtomicInteger(state.getFluidState().getAmount());
        Map<BlockPos, Integer> posList = new HashMap<>();
        posList.put(pos, 0);
        this.findConnectedFluids(level, pos, posList, currentLevel);
        if (currentLevel.get() < maxLevel) return ItemStack.EMPTY;
        for (Map.Entry<BlockPos, Integer> entry : posList.entrySet()) {
            BlockPos p = entry.getKey();
            Integer value = entry.getValue();
            if (value == 0) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 11);
            } else {
                level.setBlock(p, this.fluid.makeState(value).createLegacyBlock(), 11);
            }
        }
        return new ItemStack(this.fluid.getBucket());
    }

    // breath first search
    private void findConnectedFluids(LevelAccessor level, BlockPos pos, Map<BlockPos, Integer> remainders, AtomicInteger currentLevel) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.offer(pos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (currentLevel.get() >= maxLevel) return;
                BlockPos newPos = currentPos.relative(direction);
                if (!remainders.containsKey(newPos)) {
                    BlockState state = level.getBlockState(newPos);
                    if (state.getBlock() instanceof FiniteLiquidBlock) {
                        int l = state.getFluidState().getAmount();
                        if (l > 0) {
                            currentLevel.addAndGet(l);
                            remainders.put(newPos, Math.max(0, currentLevel.get() - maxLevel));
                            queue.offer(newPos);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return this.fluid.getPickupSound();
    }

    @Override
    public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return fluid == this.fluid;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        //top up and eventually spread around
        int currentLevel = state.getValue(MISSING_LEVELS);
        // top up
        //TODO: improve and spread to sides
        level.setBlock(pos, state.setValue(MISSING_LEVELS, fluidState.createLegacyBlock().getValue(MISSING_LEVELS)), 3);
        return false;
    }
}
