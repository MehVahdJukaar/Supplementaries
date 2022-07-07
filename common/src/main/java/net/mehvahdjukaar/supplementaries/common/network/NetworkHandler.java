package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler.NetworkDir;
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

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncGlobeDataPacket.class, ClientBoundSyncGlobeDataPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundRequestMapDataPacket.class, ServerBoundRequestMapDataPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSetBlackboardPacket.class, ServerBoundSetBlackboardPacket::new);



        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::new);






        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSetPresentPacket.class, ServerBoundSetPresentPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSetTrappedPresentPacket.class, ServerBoundSetTrappedPresentPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSendKnockbackPacket.class, ClientBoundSendKnockbackPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncSongsPacket.class, ClientBoundSyncSongsPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSetSongPacket.class, ClientBoundSetSongPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundParticlePacket.class, ClientBoundParticlePacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundOpenScreenPacket.class, ClientBoundOpenScreenPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundPlaySongNotesPacket.class, ClientBoundPlaySongNotesPacket::new);

        CHANNEL.register(PicklePacket.class, PicklePacket::buffer, PicklePacket::new);


        //I forgor what these 2 are for

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                RequestConfigReloadPacket.class, RequestConfigReloadPacket::new);

        CHANNEL.register(OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);


    }

    //add these to channel class

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