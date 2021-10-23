package net.mehvahdjukaar.supplementaries.network;


import com.mojang.text2speech.Narrator;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class SendSpeakerBlockMessagePacket {
    private final Component str;
    private final boolean narrator;

    public SendSpeakerBlockMessagePacket(FriendlyByteBuf buf) {
        this.str = buf.readComponent();
        this.narrator = buf.readBoolean();
    }

    public SendSpeakerBlockMessagePacket(Component str, boolean narrator) {
        this.str = str;
        this.narrator = narrator;
    }

    public static void buffer(SendSpeakerBlockMessagePacket message, FriendlyByteBuf buf) {
        buf.writeComponent(message.str);
        buf.writeBoolean(message.narrator);
    }

    public static void handler(SendSpeakerBlockMessagePacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        ctx.get().enqueueWork(() -> {
            // PlayerEntity player = ctx.get().getSender();


            //TODO: add @p command support
            if (message.narrator) {
                Narrator.getNarrator().say(message.str.getString(), true);
            } else {
                Minecraft.getInstance().player.sendMessage(message.str, Util.NIL_UUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}