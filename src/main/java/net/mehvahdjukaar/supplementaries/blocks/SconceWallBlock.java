package net.mehvahdjukaar.supplementaries.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Random;

public class SconceWallBlock extends WallTorchBlock {
    //TODO: make map for other blocks
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.makeCuboidShape(6D, 2.0D, 10D, 10D, 13.0D, 16.0D),
            Direction.SOUTH, Block.makeCuboidShape(6D, 2.0D, 0.0D, 10D, 13.0D, 6D),
            Direction.WEST, Block.makeCuboidShape(10D, 2.0D, 6D, 16.0D, 13.0D, 10D),
            Direction.EAST, Block.makeCuboidShape(0.0D, 2.0D, 6D, 6D, 13.0D, 10D)));

    public SconceWallBlock(Properties properties, IParticleData particleData) {
        super(properties, particleData);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        Direction direction = stateIn.get(HORIZONTAL_FACING);
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;
        Direction direction1 = direction.getOpposite();
        worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.25D * (double)direction1.getXOffset(), d1 + 0.15D, d2 + 0.25D * (double)direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(this.particleData, d0 + 0.25D * (double)direction1.getXOffset(), d1 + 0.15D, d2 + 0.25D * (double)direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(HORIZONTAL_FACING));
    }


}
