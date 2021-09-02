package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class UpdateServerTextHolderPacket {
    private final BlockPos pos;
    public final ITextComponent[] signText;
    public final int lines;

    public UpdateServerTextHolderPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.lines = buf.readInt();
        this.signText = new ITextComponent[this.lines];
        for (int i = 0; i < this.lines; ++i) {
            this.signText[i] = buf.readComponent();
        }

    }

    public UpdateServerTextHolderPacket(BlockPos pos, ITextComponent[] signText, int lines) {
        this.pos = pos;
        this.lines = lines;
        this.signText = signText;
    }

    public static void buffer(UpdateServerTextHolderPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeInt(message.lines);
        for (int i = 0; i < message.lines; ++i) {
            buf.writeComponent(message.signText[i]);
        }
    }

    public static void handler(UpdateServerTextHolderPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                TileEntity tileentity = world.getBlockEntity(message.pos);
                if (tileentity instanceof ITextHolder) {
                    ITextHolder te = (ITextHolder) tileentity;
                    if(te.getTextHolder().size == message.lines){
                        for (int i = 0; i < message.lines; ++i) {
                            te.getTextHolder().setText(i,message.signText[i]);
                        }
                    }
                    tileentity.setChanged();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}