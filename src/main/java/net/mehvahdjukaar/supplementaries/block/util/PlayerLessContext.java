package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PlayerLessContext extends BlockItemUseContext {
    public PlayerLessContext(World worldIn, @Nullable PlayerEntity playerIn, Hand handIn, ItemStack stackIn, BlockRayTraceResult rayTraceResultIn) {
        super(worldIn, playerIn, handIn, stackIn, rayTraceResultIn);
    }
}
