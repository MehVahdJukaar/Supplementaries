package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundOpenScreenPacket implements NetworkHandler.Message {
    private final BlockPos pos;

    public ClientBoundOpenScreenPacket(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
    }

    public ClientBoundOpenScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void buffer(ClientBoundOpenScreenPacket message, PacketBuffer buffer) {
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
        World level = Minecraft.getInstance().level;
        if (level != null) {
            TileEntity te = level.getBlockEntity(pos);
            if (te instanceof IScreenProvider) {
                ((IScreenProvider) te).openScreen(level, pos, Minecraft.getInstance().player);
            }
        }
    }
}