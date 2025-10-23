package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;

public class ModNetwork {

    public static void init() {
        NetworkHelper.addNetworkRegistration(
                ModNetwork::registerMessages, 5);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {

        event.registerClientBound(ClientBoundPlaySpeakerMessagePacket.CODEC);
        event.registerClientBound(ClientBoundSendLoginPacket.CODEC);
        event.registerClientBound(ClientBoundSyncTradesPacket.CODEC);
        event.registerClientBound(ClientBoundSendKnockbackPacket.CODEC);
        event.registerClientBound(ClientBoundSyncAntiqueInk.CODEC);
        event.registerClientBound(ClientBoundSetSlidingBlockEntityPacket.CODEC);
        event.registerClientBound(ClientBoundSendHourglassDataPacket.CODEC);
        event.registerClientBound(ClientBoundSendCapturedMobsPacket.CODEC);
        event.registerClientBound(ClientBoundFinalizeBookDataPacket.CODEC);
        event.registerClientBound(ClientBoundDisplayClockTimePacket.CODEC);
        event.registerClientBound(ClientBoundSetSongPacket.CODEC);
        event.registerClientBound(ClientBoundParticlePacket.CODEC);
        event.registerClientBound(ClientBoundPlaySongNotesPacket.CODEC);
        event.registerClientBound(ClientBoundOpenConfigsPacket.CODEC);
        event.registerClientBound(ClientBoundSyncAmbientLightPacket.CODEC);
        event.registerClientBound(ClientBoundFluteParrotsPacket.CODEC);
        event.registerClientBound(ClientBoundCannonballExplosionPacket.CODEC);
        event.registerClientBound(ClientBoundControlCannonPacket.CODEC);
        event.registerClientBound(ClientBoundUpdateCannonBoatPacket.CODEC);
        event.registerClientBound(ClientBoundCannonAnimationPacket.CODEC);

        event.registerServerBound(ServerBoundSetSpeakerBlockPacket.CODEC);
        event.registerServerBound(ServerBoundSetTextHolderPacket.CODEC);
        event.registerServerBound(ServerBoundRequestMapDataPacket.CODEC);
        event.registerServerBound(ServerBoundSetBlackboardPacket.CODEC);
        event.registerServerBound(ServerBoundSelectMerchantTradePacket.CODEC);
        event.registerServerBound(ServerBoundSetPresentPacket.CODEC);
        event.registerServerBound(ServerBoundSetTrappedPresentPacket.CODEC);
        event.registerServerBound(ServerBoundCycleSelectableContainerItemPacket.CODEC);
        event.registerServerBound(ServerBoundRequestConfigReloadPacket.CODEC);
        event.registerServerBound(ServerBoundSyncCannonPacket.CODEC);
        event.registerServerBound(ServerBoundRequestOpenCannonGuiMessage.CODEC);

        event.registerBidirectional(SyncEquippedQuiverPacket.CODEC);
        event.registerBidirectional(SyncPartyCreeperPacket.CODEC);
        event.registerBidirectional(PicklePacket.CODEC);
    }
}