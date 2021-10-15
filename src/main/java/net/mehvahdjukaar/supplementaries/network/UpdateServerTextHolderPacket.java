package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class UpdateServerTextHolderPacket {
    private final BlockPos pos;
    public final Component[] signText;
    public final int lines;

    public UpdateServerTextHolderPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.lines = buf.readInt();
        this.signText = new Component[this.lines];
        for (int i = 0; i < this.lines; ++i) {
            this.signText[i] = buf.readComponent();
        }

    }

    public UpdateServerTextHolderPacket(BlockPos pos, Component[] signText, int lines) {
        this.pos = pos;
        this.lines = lines;
        this.signText = signText;
    }

    public static void buffer(UpdateServerTextHolderPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeInt(message.lines);
        for (int i = 0; i < message.lines; ++i) {
            buf.writeComponent(message.signText[i]);
        }
    }

    public static void handler(UpdateServerTextHolderPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                BlockPos pos = message.pos;
                BlockEntity tileentity = world.getBlockEntity(pos);
                if (tileentity instanceof ITextHolder) {
                    ITextHolder te = (ITextHolder) tileentity;
                    if(te.getTextHolder().size == message.lines){
                        for (int i = 0; i < message.lines; ++i) {
                            te.getTextHolder().setText(i,message.signText[i]);
                        }
                    }
                    //updates client
                    BlockState state =  world.getBlockState(pos);
                    world.sendBlockUpdated(pos, state, state, 3);
                    tileentity.setChanged();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}