package net.mehvahdjukaar.supplementaries.network;


import com.mojang.text2speech.Narrator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendSpeakerBlockMessagePacket {
    private ITextComponent str;
    private boolean narrator;
    public SendSpeakerBlockMessagePacket(PacketBuffer buf) {
        this.str = buf.readTextComponent();
        this.narrator = buf.readBoolean();
    }

    public SendSpeakerBlockMessagePacket(String str, boolean narrator) {
        this.str = new StringTextComponent(str);
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
            if (message.narrator) {
                Narrator.getNarrator().say(message.str.getString(), true);
            } else {
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(message.str.getString()), Util.DUMMY_UUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}