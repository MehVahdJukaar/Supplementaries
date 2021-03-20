package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class NetworkHandler {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static int nextID() { return ID++; }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Supplementaries.MOD_ID, "splmchannel"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(), SendSpeakerBlockMessagePacket.class, SendSpeakerBlockMessagePacket::buffer,
                SendSpeakerBlockMessagePacket::new, SendSpeakerBlockMessagePacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerSpeakerBlockPacket.class, UpdateServerSpeakerBlockPacket::buffer,
                UpdateServerSpeakerBlockPacket::new, UpdateServerSpeakerBlockPacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerTextHolderPacket.class, UpdateServerTextHolderPacket::buffer,
                UpdateServerTextHolderPacket::new, UpdateServerTextHolderPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncGlobeDataPacket.class, SyncGlobeDataPacket::buffer,
                SyncGlobeDataPacket::new, SyncGlobeDataPacket::handler);

        INSTANCE.registerMessage(nextID(), RequestMapDataFromServerPacket.class,RequestMapDataFromServerPacket::buffer,
                RequestMapDataFromServerPacket::new, RequestMapDataFromServerPacket::handler);

        INSTANCE.registerMessage(nextID(), UpdateServerBlackboardPacket.class,UpdateServerBlackboardPacket::buffer,
                UpdateServerBlackboardPacket::new, UpdateServerBlackboardPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncConfigsPacket.class,SyncConfigsPacket::buffer,
                SyncConfigsPacket::new, SyncConfigsPacket::handler);

        INSTANCE.registerMessage(nextID(), SendLoginMessagePacket.class, SendLoginMessagePacket::buffer,
                SendLoginMessagePacket::new, SendLoginMessagePacket::handler);

        INSTANCE.registerMessage(nextID(), OpenConfigsPacket.class, OpenConfigsPacket::buffer,
                OpenConfigsPacket::new, OpenConfigsPacket::handler);

        INSTANCE.registerMessage(nextID(), RequestConfigReloadPacket.class, RequestConfigReloadPacket::buffer,
                RequestConfigReloadPacket::new, RequestConfigReloadPacket::handler);

    }
}