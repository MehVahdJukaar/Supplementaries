package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

public class EmptyCageItem extends BlockItem {

    public EmptyCageItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(!(entity instanceof LivingEntity))return false;
        return this.itemInteractionForEntity(stack,player, ((LivingEntity) entity),player.getActiveHand()).isSuccessOrConsume();
    }


//TODO: replace all actionresourl.SUCCESS with ActionResultType.func_233537a(World::isRemote). CONSUME=server, SUCCESS=client

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        ResourceLocation n =  entity.getType().getRegistryName();
        if(n==null)return ActionResultType.PASS;
        String name = n.toString();
        //Fireflies

        if (!ServerConfigs.cached.CAGE_ALL_MOBS && !(ServerConfigs.cached.CAGE_ALLOWED_MOBS.contains(name)||
                (ServerConfigs.cached.CAGE_ALLOWED_BABY_MOBS.contains(name)&&entity.isChild()))) {
            return ActionResultType.PASS;
        }

        if(entity instanceof SlimeEntity && ((SlimeEntity)entity).getSlimeSize()>1) return ActionResultType.PASS;

        if(player.world.isRemote)return ActionResultType.SUCCESS;
        ItemStack returnStack = new ItemStack(Registry.CAGE_ITEM);

        if (stack.hasDisplayName()) returnStack.setDisplayName(stack.getDisplayName());

        CommonUtil.createJarMobItemNBT(returnStack, entity, 1f, 0.875f);

        player.setHeldItem(hand, DrinkHelper.fill(stack.copy(),player,returnStack,false));
        //TODO: cage sound here
        player.world.playSound(null, player.getPosition(),  SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS,1,1);

        entity.remove();
        return ActionResultType.CONSUME;
    }

}
