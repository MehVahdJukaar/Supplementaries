package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class UpdateServerPresentPacket {
    private final BlockPos pos;
    private final boolean packed;
    private final String sender;
    private final String recipient;

    public UpdateServerPresentPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.packed = buf.readBoolean();
        this.recipient = buf.readUtf();
        this.sender = buf.readUtf();
    }

    public UpdateServerPresentPacket(BlockPos pos, boolean packed, String recipient, String sender) {
        this.pos = pos;
        this.packed = packed;
        this.recipient = recipient;
        this.sender = sender;
    }

    public static void buffer(UpdateServerPresentPacket message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeBoolean(message.packed);
        buf.writeUtf(message.recipient);
        buf.writeUtf(message.sender);
    }

    public static void handler(UpdateServerPresentPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            if (world != null) {
                TileEntity tileentity = world.getBlockEntity(message.pos);
                if (tileentity instanceof PresentBlockTile) {
                    PresentBlockTile present = (PresentBlockTile) tileentity;
                    world.playSound(null,message.pos, SoundEvents.VILLAGER_WORK_LEATHERWORKER, SoundCategory.BLOCKS,1,1.3f);
                    present.pack(message.recipient, message.sender, message.packed);
                    tileentity.setChanged();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}