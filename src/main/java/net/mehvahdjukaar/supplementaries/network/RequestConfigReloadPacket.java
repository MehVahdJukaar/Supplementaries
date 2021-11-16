package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestConfigReloadPacket {
    public RequestConfigReloadPacket(PacketBuffer buffer) {}
    public RequestConfigReloadPacket() {}

    public static void buffer(RequestConfigReloadPacket message, PacketBuffer buf) {}

    public static void handler(RequestConfigReloadPacket message, Supplier<NetworkEvent.Context> ctx) {
        //server
        ctx.get().enqueueWork(() -> {
            //TODO: fix confis synginc
            //ServerConfigs.loadLocal();
            ConfigHandler.sendSyncedConfigsToAllPlayers();
            ServerConfigs.cached.refresh();

        });
        ctx.get().setPacketHandled(true);
    }
}