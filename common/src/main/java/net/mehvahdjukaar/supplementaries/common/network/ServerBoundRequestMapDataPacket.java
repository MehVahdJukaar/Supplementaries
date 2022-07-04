package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.util.IMapDisplay;
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
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

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
        // server world
        Level world = Objects.requireNonNull(context.getSender()).level;

        if (world instanceof ServerLevel) {
            Player player = world.getPlayerByUUID(this.id);
            if (player instanceof ServerPlayer serverPlayer && world.getBlockEntity(this.pos) instanceof IMapDisplay tile) {
                ItemStack stack = tile.getMapStack();
                if (stack.getItem() instanceof MapItem map) {
                    MapItemSavedData data = MapItem.getSavedData(stack, world);
                    if (data != null) {
                        data.tickCarriedBy(player, stack);
                        map.update(world, player, data);
                        Packet<?> updatePacket = map.getUpdatePacket(stack, world, player);
                        if (updatePacket != null) {
                            serverPlayer.connection.send(updatePacket);
                        }
                    }
                }
            }
        }

    }
}