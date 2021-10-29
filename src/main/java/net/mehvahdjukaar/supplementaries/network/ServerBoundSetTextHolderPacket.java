package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetTextHolderPacket {
    private final BlockPos pos;
    public final Component[] signText;
    public final int size;

    public ServerBoundSetTextHolderPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.size = buf.readInt();
        this.signText = new Component[this.size];
        for (int i = 0; i < this.size; ++i) {
            this.signText[i] = buf.readComponent();
        }

    }

    public ServerBoundSetTextHolderPacket(BlockPos pos, TextHolder textHolder) {
        this.pos = pos;
        this.size = textHolder.size();
        this.signText = textHolder.getSignText();
    }

    public static void buffer(ServerBoundSetTextHolderPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeInt(message.size);
        for (int i = 0; i < message.size; ++i) {
            buf.writeComponent(message.signText[i]);
        }
    }

    public static void handler(ServerBoundSetTextHolderPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            BlockPos pos = message.pos;
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ITextHolderProvider te) {
                if (te.getTextHolder().size() == message.size) {
                    for (int i = 0; i < message.size; ++i) {
                        te.getTextHolder().setLine(i, message.signText[i]);
                    }
                }
                //updates client
                BlockState state = world.getBlockState(pos);
                world.sendBlockUpdated(pos, state, state, 3);
                tile.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}