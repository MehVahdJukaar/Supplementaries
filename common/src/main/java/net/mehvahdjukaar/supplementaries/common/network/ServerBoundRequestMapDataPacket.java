package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.IMapDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Objects;
import java.util.UUID;

public class ServerBoundRequestMapDataPacket implements Message {
    private final BlockPos pos;
    private final UUID id;

    public ServerBoundRequestMapDataPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.id = buf.readUUID();
    }

    public ServerBoundRequestMapDataPacket(BlockPos pos, UUID id) {
        this.pos = pos;
        this.id = id;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUUID(this.id);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server level
        Level level = Objects.requireNonNull(context.getSender()).level();

        if (level instanceof ServerLevel) {
            Player player = level.getPlayerByUUID(this.id);
            if (player instanceof ServerPlayer serverPlayer && level.hasChunkAt(pos) &&
                    level.getBlockEntity(this.pos) instanceof IMapDisplay tile) {
                ItemStack stack = tile.getMapStack();
                if (stack.getItem() instanceof MapItem map) {
                    MapItemSavedData data = MapItem.getSavedData(stack, level);
                    if (data != null) {
                        data.tickCarriedBy(player, stack);
                        map.update(level, player, data);
                        Packet<?> updatePacket = map.getUpdatePacket(stack, level, player);
                        if (updatePacket != null) {
                            serverPlayer.connection.send(updatePacket);
                        }
                    }
                }
            }
        }

    }
}