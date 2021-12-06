package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
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

public class ServerBoundRequestMapDataPacket {
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

    public static void buffer(ServerBoundRequestMapDataPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeUUID(message.id);
    }

    public static void handler(ServerBoundRequestMapDataPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;

        ctx.get().enqueueWork(() -> {
            if (world instanceof ServerLevel) {
                Player player = world.getPlayerByUUID(message.id);
                if (player instanceof ServerPlayer serverPlayer && world.getBlockEntity(message.pos) instanceof IMapDisplay tile) {
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
        });
        ctx.get().setPacketHandled(true);
    }
}