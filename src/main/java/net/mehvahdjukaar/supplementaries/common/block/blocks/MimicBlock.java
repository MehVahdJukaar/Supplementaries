package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.List;

public abstract class MimicBlock extends Block implements IForgeBlock {
    public MimicBlock(Properties properties) {
        super(properties);
    }

    //THIS IS DANGEROUS
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            //prevent infinite recursion
            if (!mimicState.isAir() && !(mimicState.getBlock() instanceof MimicBlock))
                return mimicState.getDestroyProgress(player, worldIn, pos);
        }
        return super.getDestroyProgress(state, player, worldIn, pos);
    }

    //might cause lag when breaking?
    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        if (world.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            if (!mimicState.isAir()) return mimicState.getSoundType(world, pos, entity);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof IBlockHolder tile) {
            List<ItemStack> newDrops = tile.getHeldBlock().getDrops(builder);
            //ItemStack camo = new ItemStack(tile.getHeldBlock().getBlock());
            drops.addAll(newDrops);
        }
        return drops;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        if (world.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            if (!mimicState.isAir()) {
                return mimicState.getExplosionResistance(world, pos, explosion);
            }
        }
        return 2;
    }

    /*
    @Override
    public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, @org.jetbrains.annotations.Nullable Entity entity) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof IBlockHolder) {
            BlockState mimicState = ((IBlockHolder) te).getHeldBlock();
            if (!mimicState.isAir()) {
                return mimicState.getSlipperiness(world, pos, entity);
            }
        }
        return super.getSlipperiness(state,world,pos,entity);
    }*/

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
}
