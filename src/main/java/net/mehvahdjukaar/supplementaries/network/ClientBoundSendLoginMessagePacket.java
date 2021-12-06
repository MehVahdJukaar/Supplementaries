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
        ctx.get().enqueueWork(() -> {

            if (ClientConfigs.general.ANTI_REPOST_WARNING.get()) {
                try {
                    String fileName = ModList.get().getModFileById(Supplementaries.MOD_ID).getFile().getFileName();
                    if (fileName.contains(".jar")) {
                        if (!fileName.toLowerCase().contains("supplementaries-1") || fileName.toLowerCase().contains("supplementaries-mod") || fileName.contains("supplementaries-1.16.53")) {
                            LocalPlayer player = Minecraft.getInstance().player;
                            MutableComponent link = new TranslatableComponent("message.supplementaries.anti_repost_link");
                            String url = "http://www.curseforge.com/minecraft/mc-mods/supplementaries";
                            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
                            link.setStyle(link.getStyle().withClickEvent(click).setUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));

                            player.sendMessage(new TranslatableComponent("message.supplementaries.anti_repost", link), Util.NIL_UUID);
                            player.sendMessage(new TranslatableComponent("message.supplementaries.anti_repost_2"), Util.NIL_UUID);
                            //player.sendMessage(ForgeHooks.newChatWithLinks(, false), Util.DUMMY_UUID);
                        }
                    }
                } catch (Exception ignored) {
                }
            }

        });
        ctx.get().setPacketHandled(true);
    }
}