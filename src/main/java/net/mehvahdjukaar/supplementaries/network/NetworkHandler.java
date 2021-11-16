package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.IndexedMessageCodec;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

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
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Supplementaries.MOD_ID, "network"), () -> PROTOCOL_VERSION,
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

        register(ClientBoundSendLoginMessagePacket.class, ClientBoundSendLoginMessagePacket::buffer,
                ClientBoundSendLoginMessagePacket::new, ClientBoundSendLoginMessagePacket::handler);

        register(OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);

        register(RequestConfigReloadPacket.class, RequestConfigReloadPacket::buffer,
                RequestConfigReloadPacket::new, RequestConfigReloadPacket::handler);

        //INSTANCE.registerMessage(nextID(), PicklePacket.class, PicklePacket::buffer,
       //         PicklePacket::new, PicklePacket::handler);

        register(ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::buffer,
                ClientBoundSyncTradesPacket::new, ClientBoundSyncTradesPacket::handler);


        register(ServerBoundSetPresentPacket.class, ServerBoundSetPresentPacket::buffer,
                ServerBoundSetPresentPacket::new, ServerBoundSetPresentPacket::handler);

        register(ClientBoundSendBombKnockbackPacket.class, ClientBoundSendBombKnockbackPacket::buffer,
                ClientBoundSendBombKnockbackPacket::new, ClientBoundSendBombKnockbackPacket::handler);

        register(ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::buffer,
                ServerBoundSelectMerchantTradePacket::new, ServerBoundSelectMerchantTradePacket::handler);

        register(ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::buffer,
                ClientBoundSyncAntiqueInk::new, ClientBoundSyncAntiqueInk::handler);

        register(ClientBoundSyncSongsPacket.class, ClientBoundSyncSongsPacket::buffer,
                ClientBoundSyncSongsPacket::new, ClientBoundSyncSongsPacket::handler);
        register(ClientBoundSetSongPacket.class, ClientBoundSetSongPacket::buffer,
                ClientBoundSetSongPacket::new, ClientBoundSetSongPacket::handler);

    }

    public static void sendToAllTrackingClients(Entity entity, ServerLevel world, Message message) {
        world.getChunkSource().broadcast(entity, INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServerPlayer(Message message) {
        Minecraft.getInstance().getConnection().send(
                NetworkHandler.INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_SERVER));
    }

    public interface Message {

    }
}