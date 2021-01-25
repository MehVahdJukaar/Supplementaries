package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.util.function.Supplier;

public class EmptyCageItem extends BlockItem {
    public final Supplier<Item> full;
    public final CageWhitelist cageType;
    public EmptyCageItem(Block blockIn, Properties properties, Supplier<Item> full, CageWhitelist whitelist) {
        super(blockIn, properties);
        this.full = full;
        this.cageType = whitelist;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(!(entity instanceof LivingEntity)||player.getActiveHand()==null)return false;
        return this.itemInteractionForEntity(stack,player, ((LivingEntity) entity),player.getActiveHand()).isSuccessOrConsume();
    }


    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        ResourceLocation n =  entity.getType().getRegistryName();
        if(n==null)return ActionResultType.PASS;
        String name = n.toString();

        boolean isFirefly = false;
        switch (this.cageType){
            case CAGE:
                if (!ServerConfigs.cached.CAGE_ALL_MOBS && !(ServerConfigs.cached.CAGE_ALLOWED_MOBS.contains(name)||
                        (ServerConfigs.cached.CAGE_ALLOWED_BABY_MOBS.contains(name)&&entity.isChild()))) {
                    return ActionResultType.PASS;
                }
                break;
            case JAR:
                isFirefly = entity.getType().getRegistryName().getPath().toLowerCase().contains("firefl");
                if (!ServerConfigs.cached.MOB_JAR_ALLOWED_MOBS.contains(name)) {
                    return ActionResultType.PASS;
                }
                break;
            case TINTED_JAR:
                if (!ServerConfigs.cached.MOB_JAR_TINTED_ALLOWED_MOBS.contains(name)) {
                    return ActionResultType.PASS;
                }
                break;
        }
        if(entity instanceof SlimeEntity && ((SlimeEntity)entity).getSlimeSize()>1) return ActionResultType.PASS;

        if(player.world.isRemote)return ActionResultType.SUCCESS;

        ItemStack returnStack = new ItemStack(isFirefly?  Registry.FIREFLY_JAR_ITEM.get() : this.full.get());

        if(!isFirefly) {

            if (stack.hasDisplayName()) returnStack.setDisplayName(stack.getDisplayName());

            MobHolder.createMobHolderItemNBT(returnStack, entity, this.cageType.height, this.cageType.width);
        }

        player.setHeldItem(hand, DrinkHelper.fill(stack.copy(),player,returnStack,false));
        //TODO: cage sound here
        player.world.playSound(null, player.getPosition(),  SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS,1,1);

        entity.remove();
        return ActionResultType.CONSUME;
    }

    //TODO: add whitelist reference in here or in item constructor. maybe ad return item here too
    public enum CageWhitelist{
        CAGE( 1f, 0.875f),
        JAR( 0.875f, 0.625f),
        TINTED_JAR( 0.875f, 0.625f);

        public final float width;
        public final float height;

        CageWhitelist(float blockH, float blockW){
            this.width = blockW;
            this.height = blockH;
        }

        public boolean isJar(){
            return this!=CAGE;
        }
    }

}
