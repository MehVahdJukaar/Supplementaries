package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.common.MobHolder;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EmptyJarItem extends BlockItem {

    public EmptyJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }


    //TODO: merge with jar

    //soul jar
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();

        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        if (player.isOnGround() && this.getItem() == Registry.EMPTY_JAR_ITEM_TINTED && EnchantmentHelper.hasSoulSpeed(player) && world.getBlockState(pos).isIn(BlockTags.SOUL_SPEED_BLOCKS)) {

            BlockPos p = new BlockPos(player.getPosX(), player.getBoundingBox().minY - 0.5000001D, player.getPosZ());
            //Vector3d motion = player.getMotion();
            //boolean b = Math.abs(motion.x)+Math.abs(motion.z)>0.01;
            if(Math.abs(p.getX()-pos.getX())<2 && Math.abs(p.getZ()-pos.getZ())<2 && pos.getY()==p.getY()){
                if(!world.isRemote) {
                    Hand hand = context.getHand();
                    player.setHeldItem(context.getHand(), DrinkHelper.fill(player.getHeldItem(hand).copy(), player, new ItemStack(Registry.SOUL_JAR_ITEM), true));
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

    //catch entity
    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(!(entity instanceof LivingEntity))return false;
        return this.itemInteractionForEntity(stack,player, ((LivingEntity) entity),player.getActiveHand()).isSuccessOrConsume();
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {

        ResourceLocation n =  entity.getType().getRegistryName();
        if(n==null)return ActionResultType.PASS;
        String name = n.toString();
        //Fireflies
        boolean flag = this.getItem() == Registry.EMPTY_JAR_ITEM;
        boolean isFirefly = entity.getType().getRegistryName().getPath().toLowerCase().contains("firefl") && flag;
        if(!isFirefly) {
            if (flag ? !ServerConfigs.cached.MOB_JAR_ALLOWED_MOBS.contains(name) :
                    !ServerConfigs.cached.MOB_JAR_TINTED_ALLOWED_MOBS.contains(name)) {
                return ActionResultType.PASS;
                //TODO: figure out diccerence between ActionResultType.SUCCESS and CONSUME
            }
        }

        if(entity instanceof SlimeEntity && ((SlimeEntity)entity).getSlimeSize()>1) return ActionResultType.PASS;

        if(player.world.isRemote)return ActionResultType.SUCCESS;
        ItemStack returnStack = new ItemStack(isFirefly?  Registry.FIREFLY_JAR_ITEM : (flag ? Registry.JAR_ITEM : Registry.JAR_ITEM_TINTED));
        if(!isFirefly) {

            if (stack.hasDisplayName()) returnStack.setDisplayName(stack.getDisplayName());

            MobHolder.createMobHolderItemNBT(returnStack, entity, 0.875f, 0.625f);
        }
        player.setHeldItem(hand, DrinkHelper.fill(stack.copy(),player,returnStack,isFirefly));
        player.world.playSound(null, player.getPosition(),  SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS,1,1);

        entity.remove();
        return ActionResultType.CONSUME;
    }

}
