package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";

    private static <MSG> void register(Class<MSG> messageClass, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        INSTANCE.registerMessage(ID++, messageClass, encoder, decoder, messageConsumer);
    }


    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(Supplementaries.res("network"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        register(ClientBoundPlaySpeakerMessagePacket.class, ClientBoundPlaySpeakerMessagePacket::buffer,
                ClientBoundPlaySpeakerMessagePacket::new, ClientBoundPlaySpeakerMessagePacket::handler);

        register(ServerBoundSetSpeakerBlockPacket.class, ServerBoundSetSpeakerBlockPacket::buffer,
                ServerBoundSetSpeakerBlockPacket::new, ServerBoundSetSpeakerBlockPacket::handler);

        register(ServerBoundSetTextHolderPacket.class, ServerBoundSetTextHolderPacket::buffer,
                ServerBoundSetTextHolderPacket::new, ServerBoundSetTextHolderPacket::handler);

        register(ClientBoundSyncGlobeDataPacket.class, ClientBoundSyncGlobeDataPacket::buffer,
                ClientBoundSyncGlobeDataPacket::new, ClientBoundSyncGlobeDataPacket::handler);

        register(ServerBoundRequestMapDataPacket.class, ServerBoundRequestMapDataPacket::buffer,
                ServerBoundRequestMapDataPacket::new, ServerBoundRequestMapDataPacket::handler);

        register(ServerBoundSetBlackboardPacket.class, ServerBoundSetBlackboardPacket::buffer,
                ServerBoundSetBlackboardPacket::new, ServerBoundSetBlackboardPacket::handler);

        register(SyncConfigsPacket.class, SyncConfigsPacket::buffer,
                SyncConfigsPacket::new, SyncConfigsPacket::handler);

        register(ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::buffer,
                ClientBoundSendLoginPacket::new, ClientBoundSendLoginPacket::handler);

        register(OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);

        register(RequestConfigReloadPacket.class, RequestConfigReloadPacket::buffer,
                RequestConfigReloadPacket::new, RequestConfigReloadPacket::handler);

        register(PicklePacket.class, PicklePacket::buffer,
                PicklePacket::new, PicklePacket::handler);

        register(ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::buffer,
                ClientBoundSyncTradesPacket::new, ClientBoundSyncTradesPacket::handler);


        register(ServerBoundSetPresentPacket.class, ServerBoundSetPresentPacket::buffer,
                ServerBoundSetPresentPacket::new, ServerBoundSetPresentPacket::handler);

        register(ServerBoundSetTrappedPresentPacket.class, ServerBoundSetTrappedPresentPacket::buffer,
                ServerBoundSetTrappedPresentPacket::new, ServerBoundSetTrappedPresentPacket::handler);

        register(ClientBoundSendKnockbackPacket.class, ClientBoundSendKnockbackPacket::buffer,
                ClientBoundSendKnockbackPacket::new, ClientBoundSendKnockbackPacket::handler);

        register(ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::buffer,
                ServerBoundSelectMerchantTradePacket::new, ServerBoundSelectMerchantTradePacket::handler);

        register(ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::buffer,
                ClientBoundSyncAntiqueInk::new, ClientBoundSyncAntiqueInk::handler);

        register(ClientBoundSyncSongsPacket.class, ClientBoundSyncSongsPacket::buffer,
                ClientBoundSyncSongsPacket::new, ClientBoundSyncSongsPacket::handler);

        register(ClientBoundSetSongPacket.class, ClientBoundSetSongPacket::buffer,
                ClientBoundSetSongPacket::new, ClientBoundSetSongPacket::handler);

        register(ClientBoundParticlePacket.class, ClientBoundParticlePacket::buffer,
                ClientBoundParticlePacket::new, ClientBoundParticlePacket::handler);

        register(ClientBoundOpenScreenPacket.class, ClientBoundOpenScreenPacket::buffer,
                ClientBoundOpenScreenPacket::new, ClientBoundOpenScreenPacket::handler);

        register(ClientBoundPlaySongNotesPacket.class, ClientBoundPlaySongNotesPacket::buffer,
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

    public interface Message {

    }
}