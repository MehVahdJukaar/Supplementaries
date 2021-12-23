package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

import java.util.Random;
import java.util.function.Supplier;

public class SconceLeverBlock extends SconceWallBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SconceLeverBlock(Properties properties, Supplier<SimpleParticleType> particleData) {
        super(properties, particleData);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    //need to update neighbours too
    //TODO: remove by replacing proper update for block change 11->3
    @Override
    public void onChange(BlockState state, LevelAccessor world, BlockPos pos) {
        if (world instanceof Level)
            this.updateNeighbors(state, (Level) world, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        InteractionResult result = super.use(state, worldIn, pos, player, handIn, hit);
        if (result.consumesAction()) {
            this.updateNeighbors(state, worldIn, pos);
            return result;
        }
        if (worldIn.isClientSide) {
            state.cycle(POWERED);
            return InteractionResult.SUCCESS;
        } else {
            BlockState blockstate = this.setPowered(state, worldIn, pos);
            boolean enabled = blockstate.getValue(POWERED);
            float f = enabled ? 0.6F : 0.5F;
            worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            worldIn.gameEvent(player, enabled ? GameEvent.BLOCK_SWITCH : GameEvent.BLOCK_UNSWITCH, pos);
            return InteractionResult.CONSUME;
        }
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
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        if (!stateIn.getValue(POWERED)) {
            super.animateTick(stateIn, worldIn, pos, rand);
        } else if (stateIn.getValue(LIT)) {
            Direction direction = stateIn.getValue(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.65D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.125D * (double) direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0 + 0.125D * (double) direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}