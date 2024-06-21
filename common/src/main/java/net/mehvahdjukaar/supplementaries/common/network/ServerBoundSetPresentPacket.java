package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ServerBoundSetPresentPacket implements Message {
    private final BlockPos pos;
    private final boolean packed;
    private final String sender;
    private final String recipient;
    private final String description;

    public ServerBoundSetPresentPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.packed = buf.readBoolean();
        this.recipient = buf.readUtf();
        this.sender = buf.readUtf();
        this.description = buf.readUtf();
    }

    public ServerBoundSetPresentPacket(BlockPos pos, boolean packed, String recipient, String sender, String description) {
        this.pos = pos;
        this.packed = packed;
        this.recipient = recipient;
        this.sender = sender;
        this.description = description;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.packed);
        buf.writeUtf(this.recipient);
        buf.writeUtf(this.sender);
        buf.writeUtf(this.description);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server level
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
        Level level = player.level();

        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof PresentBlockTile present) {
            //TODO: sound here
//TODO: check if 2 players cant edit at once of it it needs OnePlyaerInteractable
            present.updateState(this.packed, this.recipient, this.sender, this.description);

            BlockState state = level.getBlockState(pos);
            present.setChanged();
            //also sends new block to clients. maybe not needed since blockstate changes
            level.sendBlockUpdated(pos, state, state, 3);

            //if I'm packing also closes the gui
            if (this.packed) {
                player.doCloseContainer();
            }
        }

    }
}