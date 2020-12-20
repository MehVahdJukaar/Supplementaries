package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.blocks.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.IMapDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestMapDataFromServerPacket {
    private final BlockPos pos;
    private final UUID id;
    public RequestMapDataFromServerPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.id = buf.readUniqueId();
    }

    public RequestMapDataFromServerPacket(BlockPos pos, UUID id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(RequestMapDataFromServerPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeUniqueId(message.id);
    }

    public static void handler(RequestMapDataFromServerPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).world;

        ctx.get().enqueueWork(() -> {
            if (world instanceof ServerWorld) {
                TileEntity tileentity = world.getTileEntity(message.pos);
                if (tileentity instanceof IMapDisplay) {
                    ItemStack stack = ((IMapDisplay)tileentity).getMapStack();
                    PlayerEntity player = world.getPlayerByUuid(message.id);
                    if(stack.getItem() instanceof FilledMapItem){
                        MapData data =  FilledMapItem.getData(stack, world);
                        FilledMapItem map = (FilledMapItem)stack.getItem();
                        data.updateVisiblePlayers(player, stack);
                        map.updateMapData(world, player, data);
                        IPacket<?> ipacket = map.getUpdatePacket(stack, world, player);
                        if (ipacket != null && player instanceof ServerPlayerEntity) {
                            ((ServerPlayerEntity)player).connection.sendPacket(ipacket);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}