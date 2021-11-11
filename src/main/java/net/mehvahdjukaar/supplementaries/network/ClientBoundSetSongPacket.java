package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.world.songs.Song;
import net.mehvahdjukaar.supplementaries.world.songs.SongsManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSetSongPacket {
    private final ResourceLocation song;
    public ClientBoundSetSongPacket(FriendlyByteBuf buf) {
        this.song = buf.readResourceLocation();
    }

    public ClientBoundSetSongPacket(ResourceLocation resourceLocation) {
        this.song = resourceLocation;
    }

    public static void buffer(ClientBoundSetSongPacket message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.song);
    }

    public static void handler(ClientBoundSetSongPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {

            });
        }
        ctx.get().setPacketHandled(true);
    }
}