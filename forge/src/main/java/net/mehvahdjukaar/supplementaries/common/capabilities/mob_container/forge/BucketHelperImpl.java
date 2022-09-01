package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.forge;

import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BucketHelperImpl {

    public static ItemStack tryGettingFishBucketHackery(Entity entity, ServerLevel serverLevel) {
        Player player = CommonUtil.getFakePlayer(serverLevel);

        ItemStack bucket = ItemStack.EMPTY;
        //hax incoming
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        InteractionResult result = entity.interact(player, InteractionHand.MAIN_HAND);
        if (!result.consumesAction()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            result = entity.interact(player, InteractionHand.MAIN_HAND);
        }

        if (result.consumesAction()) {
            ItemStack filledBucket = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!filledBucket.isEmpty() && !entity.isAlive()) {
                bucket = filledBucket;
            }
        }
        return bucket;
    }
}
