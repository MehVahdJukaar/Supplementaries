package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestConfigReloadPacket {
    public RequestConfigReloadPacket(FriendlyByteBuf buffer) {
    }

    public RequestConfigReloadPacket() {
    }

    public static void buffer(RequestConfigReloadPacket message, FriendlyByteBuf buf) {
    }

    public static void handler(RequestConfigReloadPacket message, Supplier<NetworkEvent.Context> ctx) {
        //server
        ctx.get().enqueueWork(() -> {
            ConfigHandler.configScreenReload(ctx.get().getSender());

        });
        ctx.get().setPacketHandled(true);
    }

}