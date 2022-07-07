package net.mehvahdjukaar.supplementaries.common.events.forge;

import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEventsImpl {

    public static void init() {
        var bus = MinecraftForge.EVENT_BUS;

    }

    @SubscribeEvent(priority =  EventPriority.LOW)
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onRightClickBlock(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS){
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onUseBlockHP(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onRightClickBlockHP(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS){
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(PlayerInteractEvent.RightClickItem event){
        if(!event.isCanceled()){
            ServerEvents.onUseItem(event.getPlayer(),event.getWorld(),event.getHand());
        }
    }
}
