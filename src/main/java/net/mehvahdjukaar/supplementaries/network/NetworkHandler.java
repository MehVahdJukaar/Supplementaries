package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class NetworkHandler {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";

    public static int nextID() {
        return ID++;
    }


    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Supplementaries.MOD_ID, "network"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(), ClientBoundPlaySpeakerMessagePacket.class, ClientBoundPlaySpeakerMessagePacket::buffer,
                ClientBoundPlaySpeakerMessagePacket::new, ClientBoundPlaySpeakerMessagePacket::handler);

        INSTANCE.registerMessage(nextID(), ServerBoundSetSpeakerBlockPacket.class, ServerBoundSetSpeakerBlockPacket::buffer,
                ServerBoundSetSpeakerBlockPacket::new, ServerBoundSetSpeakerBlockPacket::handler);

        INSTANCE.registerMessage(nextID(), ServerBoundSetTextHolderPacket.class, ServerBoundSetTextHolderPacket::buffer,
                ServerBoundSetTextHolderPacket::new, ServerBoundSetTextHolderPacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncGlobeDataPacket.class, ClientBoundSyncGlobeDataPacket::buffer,
                ClientBoundSyncGlobeDataPacket::new, ClientBoundSyncGlobeDataPacket::handler);

        INSTANCE.registerMessage(nextID(), ServerBoundRequestMapDataPacket.class, ServerBoundRequestMapDataPacket::buffer,
                ServerBoundRequestMapDataPacket::new, ServerBoundRequestMapDataPacket::handler);

        INSTANCE.registerMessage(nextID(), ServerBoundSetBlackboardPacket.class, ServerBoundSetBlackboardPacket::buffer,
                ServerBoundSetBlackboardPacket::new, ServerBoundSetBlackboardPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncConfigsPacket.class, SyncConfigsPacket::buffer,
                SyncConfigsPacket::new, SyncConfigsPacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSendLoginMessagePacket.class, ClientBoundSendLoginMessagePacket::buffer,
                ClientBoundSendLoginMessagePacket::new, ClientBoundSendLoginMessagePacket::handler);

        INSTANCE.registerMessage(nextID(), OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);

        INSTANCE.registerMessage(nextID(), RequestConfigReloadPacket.class, RequestConfigReloadPacket::buffer,
                RequestConfigReloadPacket::new, RequestConfigReloadPacket::handler);

        //INSTANCE.registerMessage(nextID(), PicklePacket.class, PicklePacket::buffer,
       //         PicklePacket::new, PicklePacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncTradesPacket.class, ClientBoundSyncTradesPacket::buffer,
                ClientBoundSyncTradesPacket::new, ClientBoundSyncTradesPacket::handler);


        INSTANCE.registerMessage(nextID(), ServerBoundSetPresentPacket.class, ServerBoundSetPresentPacket::buffer,
                ServerBoundSetPresentPacket::new, ServerBoundSetPresentPacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSendBombKnockbackPacket.class, ClientBoundSendBombKnockbackPacket::buffer,
                ClientBoundSendBombKnockbackPacket::new, ClientBoundSendBombKnockbackPacket::handler);

        INSTANCE.registerMessage(nextID(), ServerBoundSelectMerchantTradePacket.class, ServerBoundSelectMerchantTradePacket::buffer,
                ServerBoundSelectMerchantTradePacket::new, ServerBoundSelectMerchantTradePacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncAntiqueInk.class, ClientBoundSyncAntiqueInk::buffer,
                ClientBoundSyncAntiqueInk::new, ClientBoundSyncAntiqueInk::handler);


    }

    public static void sendToAllTrackingClient(Entity entity, ServerLevel world, Message message) {
        world.getChunkSource().broadcast(entity, INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServerPlayer(Message message) {
        Minecraft.getInstance().getConnection().send(
                NetworkHandler.INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_SERVER));
    }

    public interface Message {

    }
}