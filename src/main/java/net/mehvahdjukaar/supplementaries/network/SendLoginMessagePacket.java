package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendLoginMessagePacket {
    public SendLoginMessagePacket(PacketBuffer buf) {}

    public SendLoginMessagePacket() {}

    public static void buffer(SendLoginMessagePacket message, PacketBuffer buf) {}

    public static void handler(SendLoginMessagePacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        ctx.get().enqueueWork(() -> {

            if(ClientConfigs.general.ANTI_REPOST_WARNING.get()) {
                try {
                    String fileName = ModList.get().getModFileById(Supplementaries.MOD_ID).getFile().getFileName();

                    if (!fileName.toLowerCase().contains("supplementaries-1")||fileName.toLowerCase().contains("supplementaries-mod")||fileName.contains("supplementaries-1.16.53")) {
                        ClientPlayerEntity player = Minecraft.getInstance().player;
                        IFormattableTextComponent link = new TranslationTextComponent("message.supplementaries.anti_repost_link");
                        String url = "http://www.curseforge.com/minecraft/mc-mods/supplementaries";
                        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
                        link.setStyle(link.getStyle().withClickEvent(click).setUnderlined(true).withColor(Color.fromLegacyFormat(TextFormatting.BLUE)));

                        player.sendMessage(new TranslationTextComponent("message.supplementaries.anti_repost",link), Util.NIL_UUID);
                        player.sendMessage(new TranslationTextComponent("message.supplementaries.anti_repost_2"), Util.NIL_UUID);
                        //player.sendMessage(ForgeHooks.newChatWithLinks(, false), Util.DUMMY_UUID);
                    }
                } catch (Exception ignored) { }
            }

        });
        ctx.get().setPacketHandled(true);
    }
}