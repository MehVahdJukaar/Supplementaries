package net.mehvahdjukaar.supplementaries.network;


import com.mojang.text2speech.Narrator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendSpeakerBlockMessagePacket {
    private final ITextComponent str;
    private final boolean narrator;
    public SendSpeakerBlockMessagePacket(PacketBuffer buf) {
        this.str = buf.readTextComponent();
        this.narrator = buf.readBoolean();
    }

    public SendSpeakerBlockMessagePacket(ITextComponent str, boolean narrator) {
        this.str = str;
        this.narrator = narrator;
    }

    public static void buffer(SendSpeakerBlockMessagePacket message, PacketBuffer buf) {
        buf.writeTextComponent(message.str);
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
                Minecraft.getInstance().player.sendMessage(message.str, Util.DUMMY_UUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}