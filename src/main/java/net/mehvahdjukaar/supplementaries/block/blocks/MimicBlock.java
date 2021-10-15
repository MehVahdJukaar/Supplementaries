package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class MimicBlock extends Block implements IForgeBlock{
    public MimicBlock(Properties properties) {
        super(properties);
    }

    //THIS IS DANGEROUS
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
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
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IBlockHolder){
            BlockState mimicState = ((IBlockHolder) te).getHeldBlock();
            if(!mimicState.isAir())return mimicState.getSoundType(world,pos,entity);
        }
        return super.getSoundType(state,world,pos,entity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity te = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (te instanceof IBlockHolder) {
            ItemStack camo = new ItemStack(((IBlockHolder) te).getHeldBlock().getBlock());
            if (!camo.isEmpty()) {
                drops.add(camo);
            }
        }
        return drops;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        BlockEntity te = world.getBlockEntity(pos);
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
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

}
