package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class EmptyJarItem extends BlockItem {

    public EmptyJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        String name =  entity.getType().getRegistryName().toString();
        if(!ServerConfigs.cached.MOB_JAR_ALLOWED_MOBS.contains(name)){
            return false;
        }

        if(entity instanceof SlimeEntity && ((SlimeEntity)entity).getSlimeSize()>1) return false;

        if(player.world.isRemote)return true;

        Entity e = entity;
        e.copyDataFromOld(entity);
        e.rotationYaw=0;
        e.prevRotationYaw=0;
        e.prevRotationPitch=0;
        e.rotationPitch=0;
        if(e instanceof LivingEntity){
            LivingEntity le = ((LivingEntity)e);
            le.prevRotationYawHead=0;
            le.rotationYawHead=0;
        }

        ItemStack returnStack = new ItemStack(Registry.JAR_ITEM);
        if(stack.hasDisplayName())returnStack.setDisplayName(stack.getDisplayName());

        CommonUtil.saveJarMobItemNBT(returnStack,e);

        player.setHeldItem(player.getActiveHand(), DrinkHelper.fill(stack,player,returnStack,false));
        player.world.playSound(null, player.getPosition(),  SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS,1,1);

        e.remove();
        return true;
    }

}
