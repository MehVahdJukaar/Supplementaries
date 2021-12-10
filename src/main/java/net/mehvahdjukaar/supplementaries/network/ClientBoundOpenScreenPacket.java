package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundOpenScreenPacket implements NetworkHandler.Message {
    private final BlockPos pos;

    public ClientBoundOpenScreenPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public ClientBoundOpenScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void buffer(ClientBoundOpenScreenPacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
    }

    public static void handler(ClientBoundOpenScreenPacket message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                //assigns data to client
                tryOpenScreen(message.pos);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void tryOpenScreen(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            if (level.getBlockEntity(pos) instanceof IScreenProvider tile) {
                tile.openScreen(level, pos, Minecraft.getInstance().player);
            }
        }
    }
}