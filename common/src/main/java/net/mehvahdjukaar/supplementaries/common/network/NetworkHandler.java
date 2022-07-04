package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler.NetworkDir;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;

public class NetworkHandler {

    public static ChannelHandler CHANNEL;


    public static void registerMessages() {

        CHANNEL = ChannelHandler.createChannel(Supplementaries.res("network"));

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundPlaySpeakerMessagePacket.class, ClientBoundPlaySpeakerMessagePacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSetSpeakerBlockPacket.class, ServerBoundSetSpeakerBlockPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSetTextHolderPacket.class, ServerBoundSetTextHolderPacket::new);

        CHANNEL.register(ClientBoundSyncGlobeDataPacket.class, ClientBoundSyncGlobeDataPacket::buffer,
                ClientBoundSyncGlobeDataPacket::new, ClientBoundSyncGlobeDataPacket::handler);

        CHANNEL.register(ServerBoundRequestMapDataPacket.class, ServerBoundRequestMapDataPacket::buffer,
                ServerBoundRequestMapDataPacket::new, ServerBoundRequestMapDataPacket::handler);

        CHANNEL.register(ServerBoundSetBlackboardPacket.class, ServerBoundSetBlackboardPacket::buffer,
                ServerBoundSetBlackboardPacket::new, ServerBoundSetBlackboardPacket::handler);

        CHANNEL.register(SyncConfigsPacket.class, SyncConfigsPacket::buffer,
                SyncConfigsPacket::new, SyncConfigsPacket::handler);

        CHANNEL.register(ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::buffer,
                ClientBoundSendLoginPacket::new, ClientBoundSendLoginPacket::handler);

        CHANNEL.register(OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);

        CHANNEL.register(RequestConfigReloadPacket.class, RequestConfigReloadPacket::buffer,
                RequestConfigReloadPacket::new, RequestConfigReloadPacket::handler);

        CHANNEL.register(PicklePacket.class, PicklePacket::buffer,
                PicklePacket::new, PicklePacket::handler);

        CHANNEL.register(ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::buffer,
                ClientBoundSyncTradesPacket::new, ClientBoundSyncTradesPacket::handler);


        CHANNEL.register(ServerBoundSetPresentPacket.class, ServerBoundSetPresentPacket::buffer,
                ServerBoundSetPresentPacket::new, ServerBoundSetPresentPacket::handler);

        CHANNEL.register(ServerBoundSetTrappedPresentPacket.class, ServerBoundSetTrappedPresentPacket::buffer,
                ServerBoundSetTrappedPresentPacket::new, ServerBoundSetTrappedPresentPacket::handler);

        CHANNEL.register(ClientBoundSendKnockbackPacket.class, ClientBoundSendKnockbackPacket::buffer,
                ClientBoundSendKnockbackPacket::new, ClientBoundSendKnockbackPacket::handler);

        CHANNEL.register(ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::buffer,
                ServerBoundSelectMerchantTradePacket::new, ServerBoundSelectMerchantTradePacket::handler);

        CHANNEL.register(ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::buffer,
                ClientBoundSyncAntiqueInk::new, ClientBoundSyncAntiqueInk::handler);

        CHANNEL.register(ClientBoundSyncSongsPacket.class, ClientBoundSyncSongsPacket::buffer,
                ClientBoundSyncSongsPacket::new, ClientBoundSyncSongsPacket::handler);

        CHANNEL.register(ClientBoundSetSongPacket.class, ClientBoundSetSongPacket::buffer,
                ClientBoundSetSongPacket::new, ClientBoundSetSongPacket::handler);

        CHANNEL.register(ClientBoundParticlePacket.class, ClientBoundParticlePacket::buffer,
                ClientBoundParticlePacket::new, ClientBoundParticlePacket::handler);

        CHANNEL.register(ClientBoundOpenScreenPacket.class, ClientBoundOpenScreenPacket::buffer,
                ClientBoundOpenScreenPacket::new, ClientBoundOpenScreenPacket::handler);

        CHANNEL.register(ClientBoundPlaySongNotesPacket.class, ClientBoundPlaySongNotesPacket::buffer,
                ClientBoundPlaySongNotesPacket::new, ClientBoundPlaySongNotesPacket::handler);

    }

    public static void sendToAllTrackingClients(Entity entity, ServerLevel world, Message message) {
        world.getChunkSource().broadcast(entity, INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToAllInRangeClients(BlockPos pos, ServerLevel level, double distance, Message message) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            PlayerList players = currentServer.getPlayerList();
            var dimension = level.dimension();
            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(),
                    distance,
                    dimension, INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServerPlayer(Message message) {
        Minecraft.getInstance().getConnection().send(
                INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_SERVER));
    }


    //TODO: check out how these work internally

}