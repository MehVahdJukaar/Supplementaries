package net.mehvahdjukaar.supplementaries.common.events;


import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.common.utils.MovableFakePlayer;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;


public class ServerEvents {

    //block placement should stay low in priority to allow other more important mod interaction that use the event
    @EventCalled
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) { //is this check even needed?
            return ItemsOverrideHandler.tryPerformClickedBlockOverride(player, level, hand, hitResult, false);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResult onRightClickBlockHP(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) {
            return ItemsOverrideHandler.tryHighPriorityClickedBlockOverride(player, level, hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isSpectator()) {
            return ItemsOverrideHandler.tryPerformClickedItemOverride(player, level, hand, stack);
        }
        return InteractionResultHolder.pass(stack);
    }


    @EventCalled
    public static void onPlayerLoggedIn(ServerPlayer player) {
        GlobeData.sendGlobeData(player);
    }

    @EventCalled
    public static InteractionResult onRightClickEntity(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (player.isSpectator()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof FluteItem) {
            if (FluteItem.interactWithPet(stack, player, entity, hand)) {
                return InteractionResult.SUCCESS; // we need this for event to be actually cancelled
            }
        } else if (stack.getItem() instanceof AbstractMobContainerItem containerItem) {
            if (!containerItem.isFull(stack)) {
                var res = containerItem.doInteract(stack, player, entity, hand);
                if (res.consumesAction()) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static void onWorldUnload(MinecraftServer minecraftServer, ServerLevel serverLevel) {
        MovableFakePlayer.unloadLevel(serverLevel);
    }


}
