package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.util.TextHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ServerBoundSetTextHolderPacket implements Message {
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

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.size);
        for (int i = 0; i < this.size; ++i) {
            buf.writeComponent(this.signText[i]);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world
        Level world = Objects.requireNonNull(context.getSender()).level;

        BlockPos pos = this.pos;
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITextHolderProvider te) {
            if (te.getTextHolder().size() == this.size) {
                for (int i = 0; i < this.size; ++i) {
                    te.getTextHolder().setLine(i, this.signText[i]);
                }
            }
            //updates client
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, 3);
            tile.setChanged();
        }

    }
}