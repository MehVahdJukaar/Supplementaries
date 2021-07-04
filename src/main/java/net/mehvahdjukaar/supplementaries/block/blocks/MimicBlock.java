package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.List;

public abstract class MimicBlock extends Block implements IForgeBlock{
    public MimicBlock(Properties properties) {
        super(properties);
    }

    //THIS IS DANGEROUS
    @Override
    public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof IBlockHolder){
            BlockState mimicState = ((IBlockHolder) te).getHeldBlock();
            //prevent infinite recursion
            if(!mimicState.isAir()&&!(mimicState.getBlock() instanceof MimicBlock))
                return mimicState.getDestroyProgress(player,worldIn,pos);
        }
        return super.getDestroyProgress(state, player, worldIn, pos);
    }

    //might cause lag when breaking?
    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof IBlockHolder){
            BlockState mimicState = ((IBlockHolder) te).getHeldBlock();
            if(!mimicState.isAir())return mimicState.getSoundType(world,pos,entity);
        }
        return super.getSoundType(state,world,pos,entity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        TileEntity te = builder.getParameter(LootParameters.BLOCK_ENTITY);
        if (te instanceof IBlockHolder) {
            ItemStack camo = new ItemStack(((IBlockHolder) te).getHeldBlock().getBlock());
            if (!camo.isEmpty()) {
                drops.add(camo);
            }
        }
        return drops;
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof IBlockHolder) {
            BlockState mimicState = ((IBlockHolder) te).getHeldBlock();
            if (!mimicState.isAir()) {
                return mimicState.getExplosionResistance(world,pos,explosion);
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
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

}
