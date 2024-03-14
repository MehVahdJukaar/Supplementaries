package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidInteractionRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// diff property means we need a diff class
public class FiniteLiquidBlock  extends Block implements BucketPickup {

    public static final VoxelShape STABLE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final int MAX_LEVEL = 13;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    private final List<FluidState> stateCache;
    private final Supplier<? extends FlowingFluid> supplier;
    private boolean fluidStateCacheInitialized = false;


    public FiniteLiquidBlock(Supplier<? extends FlowingFluid> supplier, BlockBehaviour.Properties arg) {
        super( arg);
        this.supplier =supplier;
        this.stateCache = Lists.newArrayList();
        this.registerDefaultState((this.stateDefinition.any()).setValue(LEVEL, 0));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var i = pos.getCenter();
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER,
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,
                        MAX_LEVEL - state.getValue(LEVEL))), i.x, i.y, i.z, 0.0, 0.0, 0.0);
        if (state.getFluidState().getValue(FiniteFluid.FALLING)) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER,
                    Blocks.COBWEB.defaultBlockState()), i.x, i.y + 1, i.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        int i = state.getValue(LEVEL);
        if (!this.fluidStateCacheInitialized) {
            this.initFluidStateCache();
        }

        return this.stateCache.get(Math.min(i, MAX_LEVEL));
    }


    protected synchronized void initFluidStateCache() {
        if (!this.fluidStateCacheInitialized) {
            this.stateCache.add(this.getFluid().getSource(false));

            for (int i = 1; i < MAX_LEVEL; ++i) {
                this.stateCache.add(this.getFluid().getFlowing(MAX_LEVEL - i, false));
            }
            this.stateCache.add(this.getFluid().getFlowing(MAX_LEVEL, true));
            this.fluidStateCacheInitialized = true;
        }

    }

    private FlowingFluid getFluid() {
        return supplier.get();
    }









    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isAbove(STABLE_SHAPE, pos, true) && (Integer)state.getValue(LEVEL) == 0 && context.canStandOnFluid(level.getFluidState(pos.above()), state.getFluidState()) ? STABLE_SHAPE : Shapes.empty();
    }

    public boolean isRandomlyTicking(BlockState state) {
        return state.getFluidState().isRandomlyTicking();
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        state.getFluidState().randomTick(level, pos, random);
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }

    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction direction) {
        return adjacentBlockState.getFluidState().getType().isSame(this.getFluid());
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public List<ItemStack> getDrops(BlockState arg, LootParams.Builder arg2) {
        return Collections.emptyList();
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!FluidInteractionRegistry.canInteract(level, pos)) {
            level.scheduleTick(pos, state.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }

    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getFluidState().isSource() || neighborState.getFluidState().isSource()) {
            level.scheduleTick(currentPos, state.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!FluidInteractionRegistry.canInteract(level, pos)) {
            level.scheduleTick(pos, state.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }

    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if ((Integer)state.getValue(LEVEL) == 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.getFluid().getBucket());
        } else {
            return ItemStack.EMPTY;
        }
    }


    public Optional<SoundEvent> getPickupSound() {
        return this.getFluid().getPickupSound();
    }

}
