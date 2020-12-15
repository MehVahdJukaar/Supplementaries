package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

public class EmptyJarItem extends BlockItem {

    public EmptyJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
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

            CommonUtil.createJarMobItemNBT(returnStack, entity, 0.875f, 0.625f);
        }
        player.setHeldItem(player.getActiveHand(), DrinkHelper.fill(stack.copy(),player,returnStack,isFirefly));
        player.world.playSound(null, player.getPosition(),  SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS,1,1);

        entity.remove();
        return ActionResultType.SUCCESS;
    }

}
