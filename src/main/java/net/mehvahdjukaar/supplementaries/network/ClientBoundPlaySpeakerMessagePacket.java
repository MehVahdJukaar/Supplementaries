package net.mehvahdjukaar.supplementaries.network;


import com.mojang.text2speech.Narrator;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundPlaySpeakerMessagePacket implements NetworkHandler.Message {
    private final Component str;
    private final boolean narrator;

    public ClientBoundPlaySpeakerMessagePacket(FriendlyByteBuf buf) {
        this.str = buf.readComponent();
        this.narrator = buf.readBoolean();
    }

    public ClientBoundPlaySpeakerMessagePacket(Component str, boolean narrator) {
        this.str = str;
        this.narrator = narrator;
    }

    public static void buffer(ClientBoundPlaySpeakerMessagePacket message, FriendlyByteBuf buf) {
        buf.writeComponent(message.str);
        buf.writeBoolean(message.narrator);
    }

    public static void handler(ClientBoundPlaySpeakerMessagePacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        ctx.get().enqueueWork(() -> {
            // PlayerEntity player = ctx.get().getSender();


            //TODO: add @p command support
            if (message.narrator && !ClientConfigs.cached.SPEAKER_BLOCK_MUTE) {
                Narrator.getNarrator().say(message.str.getString(), true);
            } else {
                Minecraft.getInstance().player.sendMessage(message.str, Util.NIL_UUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}