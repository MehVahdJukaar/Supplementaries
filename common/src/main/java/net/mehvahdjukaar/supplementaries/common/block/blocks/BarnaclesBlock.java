package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class BarnaclesBlock extends MultifaceBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    public static final MapCodec<BarnaclesBlock> CODEC = simpleCodec(BarnaclesBlock::new);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final MultifaceSpreader spreader = new MultifaceSpreader(new UnderwaterSpreadConfig(this));

    public BarnaclesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<BarnaclesBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(WATERLOGGED) && random.nextInt(10) == 0) {
            Direction[] directions = MultifaceBlock.availableFaces(state).toArray(Direction[]::new);
            Direction dir = directions[random.nextInt(directions.length)];
            ParticleUtils.spawnParticleOnFace(level, pos, dir, ParticleTypes.BUBBLE, Vec3.ZERO, 0.1f);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return !useContext.getItemInHand().is(ModRegistry.BARNACLES.get().asItem()) || super.canBeReplaced(state, useContext);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (!state.getValue(WATERLOGGED)) return false;
        return Direction.stream().anyMatch((arg4) -> this.spreader.canSpreadInAnyDirection(state, level, pos, arg4.getOpposite()));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }


    private static class UnderwaterSpreadConfig extends MultifaceSpreader.DefaultSpreaderConfig {

        public UnderwaterSpreadConfig(MultifaceBlock block) {
            super(block);
        }

        @Override
        public boolean canSpreadInto(BlockGetter level, BlockPos pos, MultifaceSpreader.SpreadPos spreadPos) {
            if (level.getFluidState(spreadPos.pos()).getType() != Fluids.WATER) {
                return false;
            }
            return super.canSpreadInto(level, pos, spreadPos);
        }
    }

}
