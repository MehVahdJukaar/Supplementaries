package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.FireflyJarBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;


import net.minecraft.block.AbstractBlock.Properties;

public class FireflyJarBlock extends Block {
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.box(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D),
            VoxelShapes.box(0.3125, 0.875, 0.3125, 0.6875, 1, 0.6875));

    protected final boolean soul;

    public FireflyJarBlock(Properties properties, boolean isSoul) {
        super(properties);
        this.soul=isSoul;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }


    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        super.animateTick(state, world, pos, random);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FireflyJarBlockTile(soul);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    /*
    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        world.getPendingBlockTicks().scheduleTick(pos, this, 1);
        int p = ClientConfigs.cached.FIREFLY_SPAWN_PERIOD;
        float c = (float) ClientConfigs.cached.FIREFLY_SPAWN_CHANCE;
        if(world.getGameTime() % p == 0L && rand.nextFloat() > c) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            double pr = 0.15;
            if(soul){
                pr=0.25;
                for (int l = 0; l < 1; ++l) {
                    double d0 = (x + 0.5 + (rand.nextFloat() - 0.5) * (0.625D - pr));
                    double d1 = (y + 0.25);
                    double d2 = (z + 0.5 + (rand.nextFloat() - 0.5) * (0.625D - pr));
                    world.spawnParticle(ParticleTypes.SOUL,d0,d1,d2,0,0,0,0,0);
                    //world.addParticle(ParticleTypes.SOUL, d0, d1, d2, 0, rand.nextFloat()*0.02, 0);
                }
            }
            else {
                for (int l = 0; l < 1; ++l) {
                    double d0 = (x + 0.5 + (rand.nextFloat() - 0.5) * (0.625D - pr));
                    double d1 = (y + 0.5 - 0.0625 + (rand.nextFloat() - 0.5) * (0.875D - pr));
                    double d2 = (z + 0.5 + (rand.nextFloat() - 0.5) * (0.625D - pr));
                    world.spawnParticle(Registry.FIREFLY_GLOW.get(), d0, d1, d2, 0, 0, 0, 0,0);
                    //world.addParticle(Registry.FIREFLY_GLOW.get(), d0, d1, d2, 0, 0, 0);
                }
            }

        }
    }*/

}