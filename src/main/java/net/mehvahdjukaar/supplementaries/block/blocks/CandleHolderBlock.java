package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class CandleHolderBlock extends SconceWallBlock {

    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(6D, 2.0D, 11D, 10D, 13.0D, 16.0D),
            Direction.SOUTH, Block.box(6D, 2.0D, 0.0D, 10D, 13.0D, 5D),
            Direction.WEST, Block.box(11D, 2.0D, 6D, 16.0D, 13.0D, 10D),
            Direction.EAST, Block.box(0.0D, 2.0D, 6D, 5D, 13.0D, 10D)));

    public CandleHolderBlock(Properties properties, Supplier<BasicParticleType> particleData) {
        super(properties, particleData);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(LIT, true));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return ServerConfigs.cached.CANDLE_HOLDER_LIGHT;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override

    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(stateIn.getValue(LIT)){
            Direction direction = stateIn.getValue(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.8;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.3125 * (double) direction1.getStepX(), d1, d2 + 0.3125 * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0 + 0.3125 * (double) direction1.getStepX(), d1, d2 + 0.3125 * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }

}
