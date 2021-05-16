package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
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
        this.id = buf.readUUID();
    }

    public RequestMapDataFromServerPacket(BlockPos pos, UUID id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(RequestMapDataFromServerPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeUUID(message.id);
    }

    public static void handler(RequestMapDataFromServerPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).level;

        ctx.get().enqueueWork(() -> {
            if (world instanceof ServerWorld) {
                TileEntity tileentity = world.getBlockEntity(message.pos);
                if (tileentity instanceof IMapDisplay) {
                    ItemStack stack = ((IMapDisplay)tileentity).getMapStack();
                    PlayerEntity player = world.getPlayerByUUID(message.id);
                    if(stack.getItem() instanceof FilledMapItem){
                        MapData data =  FilledMapItem.getOrCreateSavedData(stack, world);
                        if(data!=null) {
                            FilledMapItem map = (FilledMapItem) stack.getItem();
                            data.tickCarriedBy(player, stack);
                            map.update(world, player, data);
                            IPacket<?> ipacket = map.getUpdatePacket(stack, world, player);
                            if (ipacket != null && player instanceof ServerPlayerEntity) {
                                ((ServerPlayerEntity) player).connection.send(ipacket);
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}