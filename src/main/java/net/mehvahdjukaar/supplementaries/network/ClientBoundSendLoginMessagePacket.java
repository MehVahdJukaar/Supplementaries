package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSendLoginMessagePacket {
    public ClientBoundSendLoginMessagePacket(FriendlyByteBuf buf) {
    }


    public ClientBoundSendLoginMessagePacket() {
    }

    public static void buffer(ClientBoundSendLoginMessagePacket message, FriendlyByteBuf buf) {
    }

    public static void handler(ClientBoundSendLoginMessagePacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSendLoginMessagePacket(message);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}