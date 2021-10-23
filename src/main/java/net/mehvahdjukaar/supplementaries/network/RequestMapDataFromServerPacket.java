package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestMapDataFromServerPacket {
    private final BlockPos pos;
    private final UUID id;
    public RequestMapDataFromServerPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.id = buf.readUUID();
    }

    public RequestMapDataFromServerPacket(BlockPos pos, UUID id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(RequestMapDataFromServerPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeUUID(message.id);
    }

    public static void handler(RequestMapDataFromServerPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        Level world = Objects.requireNonNull(ctx.get().getSender()).level;

        ctx.get().enqueueWork(() -> {
            if (world instanceof ServerLevel) {
                BlockEntity tileentity = world.getBlockEntity(message.pos);
                if (tileentity instanceof IMapDisplay) {
                    ItemStack stack = ((IMapDisplay)tileentity).getMapStack();
                    Player player = world.getPlayerByUUID(message.id);
                    if(stack.getItem() instanceof MapItem){
                        MapItemSavedData data =  MapItem.getOrCreateSavedData(stack, world);
                        if(data!=null) {
                            MapItem map = (MapItem) stack.getItem();
                            data.tickCarriedBy(player, stack);
                            map.update(world, player, data);
                            Packet<?> ipacket = map.getUpdatePacket(stack, world, player);
                            if (ipacket != null && player instanceof ServerPlayer) {
                                ((ServerPlayer) player).connection.send(ipacket);
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}