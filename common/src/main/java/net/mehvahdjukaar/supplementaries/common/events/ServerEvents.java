package net.mehvahdjukaar.supplementaries.common.events;


import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.MovableFakePlayer;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.mehvahdjukaar.supplementaries.common.world.songs.FluteSongsReloadListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


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
        try {
            NetworkHandler.CHANNEL.sendToClientPlayer(player,
                    new ClientBoundSendLoginPacket(UsernameCache.getMap()));
        } catch (Exception exception) {
            Supplementaries.LOGGER.warn("failed to end login message: " + exception);
        }
        GlobeData.sendGlobeData(player);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new FluteSongsReloadListener());
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileImpact(final ProjectileImpactEvent event) {
        PearlMarker.onProjectileImpact(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerLevel serverLevel)
            MovableFakePlayer.unloadLevel(serverLevel);
    }


}
