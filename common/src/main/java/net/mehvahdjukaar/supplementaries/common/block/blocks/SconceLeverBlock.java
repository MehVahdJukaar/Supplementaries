package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SconceLeverBlock extends SconceWallBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SconceLeverBlock(Properties properties, Supplier<SimpleParticleType> particleData) {
        super(properties, particleData);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            state.cycle(POWERED);
            return InteractionResult.SUCCESS;
        } else {
            BlockState blockstate = this.setPowered(state, level, pos);
            boolean enabled = blockstate.getValue(POWERED);
            float f = enabled ? 0.6F : 0.5F;
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            level.gameEvent(player, enabled ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var r = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (r.consumesAction()) {
            this.updateNeighbors(state, level, pos);

        }
        return r;
    }

    public BlockState setPowered(BlockState state, Level world, BlockPos pos) {
        state = state.cycle(POWERED);
        world.setBlock(pos, state, 3);
        this.updateNeighbors(state, world, pos);
        return state;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            if (state.getValue(POWERED)) {
                this.updateNeighbors(state, worldIn, pos);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ^ !blockState.getValue(LIT) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ^ !blockState.getValue(LIT) && getFacing(blockState) == side ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, Level world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        world.updateNeighborsAt(pos.relative(getFacing(state).getOpposite()), this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    protected static Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (!stateIn.getValue(POWERED)) {
            super.animateTick(stateIn, worldIn, pos, rand);
        } else if (stateIn.getValue(LIT)) {
            Direction direction = stateIn.getValue(FACING);
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + 0.65D;
            double d2 = pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.125D * direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0 + 0.125D * direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean lightUp(Entity entity, BlockState state, BlockPos pos, LevelAccessor world, FireSoundType fireSourceType) {
        boolean ret = super.lightUp(entity, state, pos, world, fireSourceType);
        if (ret && world instanceof ServerLevel level) updateNeighbors(state, level, pos);
        return ret;
    }

    @Override
    public boolean extinguish(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world) {
        boolean ret = super.extinguish(player, state, pos, world);
        if (ret && world instanceof ServerLevel level) updateNeighbors(state, level, pos);
        return ret;
    }
}