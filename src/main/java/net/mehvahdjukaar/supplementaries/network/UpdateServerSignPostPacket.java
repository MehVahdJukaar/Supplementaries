package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.blocks.SignPostBlockTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateServerSignPostPacket {
    private BlockPos pos;
    private ITextComponent t0;
    private ITextComponent t1;

    public UpdateServerSignPostPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.t0 = buf.readTextComponent();
        this.t1 = buf.readTextComponent();
    }

    public UpdateServerSignPostPacket(BlockPos pos, ITextComponent t0, ITextComponent t1) {
        this.pos = pos;
        this.t0 = t0;
        this.t1 = t1;
    }

    public static void buffer(UpdateServerSignPostPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeTextComponent(message.t0);
        buf.writeTextComponent(message.t1);
    }

    public static void handler(UpdateServerSignPostPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = ctx.get().getSender().world;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                TileEntity tileentity = world.getTileEntity(message.pos);
                if (tileentity instanceof SignPostBlockTile) {
                    SignPostBlockTile sign = (SignPostBlockTile) tileentity;
                    sign.setText(0, message.t0);
                    sign.setText(1, message.t1);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}