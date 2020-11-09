package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.blocks.HangingSignBlockTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;


public class UpdateServerHangingSignPacket {
    private final BlockPos pos;
    private final ITextComponent t0;
    private final ITextComponent t1;
    private final ITextComponent t2;
    private final ITextComponent t3;
    private final ITextComponent t4;

    public UpdateServerHangingSignPacket(PacketBuffer buf) {

        this.pos = buf.readBlockPos();
        this.t0 = buf.readTextComponent();
        this.t1 = buf.readTextComponent();
        this.t2 = buf.readTextComponent();
        this.t3 = buf.readTextComponent();
        this.t4 = buf.readTextComponent();
    }

    public UpdateServerHangingSignPacket(BlockPos pos, ITextComponent t0, ITextComponent t1, ITextComponent t2, ITextComponent t3,
                                         ITextComponent t4) {
        this.pos = pos;
        this.t0 = t0;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
    }

    public static void buffer(UpdateServerHangingSignPacket message, PacketBuffer buf) {

        buf.writeBlockPos(message.pos);
        buf.writeTextComponent(message.t0);
        buf.writeTextComponent(message.t1);
        buf.writeTextComponent(message.t2);
        buf.writeTextComponent(message.t3);
        buf.writeTextComponent(message.t4);
    }

    public static void handler(UpdateServerHangingSignPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).world;

        ctx.get().enqueueWork(() -> {
            if (world != null) {
                TileEntity tileentity = world.getTileEntity(message.pos);
                if (tileentity instanceof HangingSignBlockTile) {
                    HangingSignBlockTile sign = (HangingSignBlockTile) tileentity;
                    sign.setText(0, message.t0);
                    sign.setText(1, message.t1);
                    sign.setText(2, message.t2);
                    sign.setText(3, message.t3);
                    sign.setText(4, message.t4);
                    tileentity.markDirty();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}