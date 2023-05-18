package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.supplementaries.Supplementaries;

public class NetworkHandler {

    public static final ChannelHandler CHANNEL = ChannelHandler.createChannel(Supplementaries.res("network"));

    public static void registerMessages() {


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

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::new);

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


        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncSongsPacket.class, ClientBoundSyncSongsPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncHourglassPacket.class, ClientBoundSyncHourglassPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncCapturedMobsPacket.class, ClientBoundSyncCapturedMobsPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSetSongPacket.class, ClientBoundSetSongPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundParticlePacket.class, ClientBoundParticlePacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::new);



        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundPlaySongNotesPacket.class, ClientBoundPlaySongNotesPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                OpenConfigsPacket.class, OpenConfigsPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                PicklePacket.ServerBound.class, PicklePacket.ServerBound::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                PicklePacket.ClientBound.class, PicklePacket.ClientBound::new);

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                ServerBoundCycleQuiverPacket.class, ServerBoundCycleQuiverPacket::new);


        CHANNEL.register(NetworkDir.PLAY_TO_SERVER,
                RequestConfigReloadPacket.class, RequestConfigReloadPacket::new);

    }

}