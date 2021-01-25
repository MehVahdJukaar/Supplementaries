package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class EmptyJarItem extends EmptyCageItem {
    public EmptyJarItem(Block blockIn, Properties properties, Supplier<Item> full, CageWhitelist whitelist) {
        super(blockIn, properties, full, whitelist);
    }

    //TODO: merge with full jars?

    private static boolean isSoulSand(BlockState s){
        try {
            return (BlockTags.SOUL_SPEED_BLOCKS != null && s.isIn(BlockTags.SOUL_SPEED_BLOCKS));
        }catch (Exception ignored){ }
        return false;
    }

    //soul jar
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();

        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        if (player.isOnGround() && this.getItem() == Registry.EMPTY_JAR_ITEM_TINTED.get() && EnchantmentHelper.hasSoulSpeed(player) && isSoulSand(world.getBlockState(pos))) {

            BlockPos p = new BlockPos(player.getPosX(), player.getBoundingBox().minY - 0.5000001D, player.getPosZ());
            //Vector3d motion = player.getMotion();
            //boolean b = Math.abs(motion.x)+Math.abs(motion.z)>0.01;
            if(Math.abs(p.getX()-pos.getX())<2 && Math.abs(p.getZ()-pos.getZ())<2 && pos.getY()==p.getY()){
                if(!world.isRemote) {
                    Hand hand = context.getHand();
                    player.setHeldItem(context.getHand(), DrinkHelper.fill(player.getHeldItem(hand).copy(), player, new ItemStack(Registry.SOUL_JAR_ITEM.get()), true));
                    player.world.playSound(null, player.getPosition(), SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 1, 1);
                    player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_SOUL_SAND_BREAK, SoundCategory.BLOCKS, 1f, 1.3f);
                    player.world.playSound(null, player.getPosition(), SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, 0.8f, 1.5f);
                    return ActionResultType.CONSUME;
                }
                return ActionResultType.SUCCESS;
            }

        }
        return super.onItemUse(context);
    }

}
