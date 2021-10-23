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

        INSTANCE.registerMessage(nextID(), SendSpeakerBlockMessagePacket.class, SendSpeakerBlockMessagePacket::buffer,
                SendSpeakerBlockMessagePacket::new, SendSpeakerBlockMessagePacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerSpeakerBlockPacket.class, UpdateServerSpeakerBlockPacket::buffer,
                UpdateServerSpeakerBlockPacket::new, UpdateServerSpeakerBlockPacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerTextHolderPacket.class, UpdateServerTextHolderPacket::buffer,
                UpdateServerTextHolderPacket::new, UpdateServerTextHolderPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncGlobeDataPacket.class, SyncGlobeDataPacket::buffer,
                SyncGlobeDataPacket::new, SyncGlobeDataPacket::handler);

        INSTANCE.registerMessage(nextID(), RequestMapDataFromServerPacket.class, RequestMapDataFromServerPacket::buffer,
                RequestMapDataFromServerPacket::new, RequestMapDataFromServerPacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerBlackboardPacket.class, UpdateServerBlackboardPacket::buffer,
                UpdateServerBlackboardPacket::new, UpdateServerBlackboardPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncConfigsPacket.class, SyncConfigsPacket::buffer,
                SyncConfigsPacket::new, SyncConfigsPacket::handler);

        INSTANCE.registerMessage(nextID(), SendLoginMessagePacket.class, SendLoginMessagePacket::buffer,
                SendLoginMessagePacket::new, SendLoginMessagePacket::handler);

        INSTANCE.registerMessage(nextID(), OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);

        INSTANCE.registerMessage(nextID(), RequestConfigReloadPacket.class, RequestConfigReloadPacket::buffer,
                RequestConfigReloadPacket::new, RequestConfigReloadPacket::handler);

        INSTANCE.registerMessage(nextID(), PicklePacket.class, PicklePacket::buffer,
                PicklePacket::new, PicklePacket::handler);

        INSTANCE.registerMessage(nextID(), SendOrangeTraderOffersPacket.class, SendOrangeTraderOffersPacket::buffer,
                SendOrangeTraderOffersPacket::new, SendOrangeTraderOffersPacket::handler);

        INSTANCE.registerMessage(nextID(), NosePacket.class, NosePacket::buffer,
                NosePacket::new, NosePacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerPresentPacket.class, UpdateServerPresentPacket::buffer,
                UpdateServerPresentPacket::new, UpdateServerPresentPacket::handler);

        INSTANCE.registerMessage(nextID(), BombExplosionKnockbackPacket.class, BombExplosionKnockbackPacket::buffer,
                BombExplosionKnockbackPacket::new, BombExplosionKnockbackPacket::handler);

        INSTANCE.registerMessage(nextID(), SelectOrangeTraderTradePacket.class, SelectOrangeTraderTradePacket::buffer,
                SelectOrangeTraderTradePacket::new, SelectOrangeTraderTradePacket::handler);


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