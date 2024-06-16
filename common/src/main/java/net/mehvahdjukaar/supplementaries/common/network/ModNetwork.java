package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.supplementaries.Supplementaries;

public class ModNetwork {

    public static final ChannelHandler CHANNEL = ChannelHandler.builder(Supplementaries.MOD_ID)
            .version(3)

            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundPlaySpeakerMessagePacket.class, ClientBoundPlaySpeakerMessagePacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncGlobeDataPacket.class, ClientBoundSyncGlobeDataPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSendKnockbackPacket.class, ClientBoundSendKnockbackPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncSongsPacket.class, ClientBoundSyncSongsPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncHourglassPacket.class, ClientBoundSyncHourglassPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncCapturedMobsPacket.class, ClientBoundSyncCapturedMobsPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSetSongPacket.class, ClientBoundSetSongPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundParticlePacket.class, ClientBoundParticlePacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundPlaySongNotesPacket.class, ClientBoundPlaySongNotesPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundOpenConfigsPacket.class, ClientBoundOpenConfigsPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSyncAmbientLightPacket.class, ClientBoundSyncAmbientLightPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundFluteParrotsPacket.class, ClientBoundFluteParrotsPacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundExplosionPacket.class, ClientBoundExplosionPacket::fromBuffer)

            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetSpeakerBlockPacket.class, ServerBoundSetSpeakerBlockPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetTextHolderPacket.class, ServerBoundSetTextHolderPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundRequestMapDataPacket.class, ServerBoundRequestMapDataPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetBlackboardPacket.class, ServerBoundSetBlackboardPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetPresentPacket.class, ServerBoundSetPresentPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetTrappedPresentPacket.class, ServerBoundSetTrappedPresentPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundCycleSelectableContainerItemPacket.class, ServerBoundCycleSelectableContainerItemPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundRequestConfigReloadPacket.class, ServerBoundRequestConfigReloadPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSyncCannonPacket.class, ServerBoundSyncCannonPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundRequestOpenCannonGuiMessage.class, ServerBoundRequestOpenCannonGuiMessage::new)

            .register(NetworkDir.BOTH, SyncSkellyQuiverPacket.class, SyncSkellyQuiverPacket::new)
            .register(NetworkDir.BOTH, PicklePacket.class, PicklePacket::new)


            .build();


    public static void init(){}
}