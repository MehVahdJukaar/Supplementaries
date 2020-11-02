package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.blocks.SpeakerBlockTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateServerSpeakerBlockPacket{
    private BlockPos pos;
    private ITextComponent str;
    private boolean narrator;

    public UpdateServerSpeakerBlockPacket(PacketBuffer buf) {

        this.pos = buf.readBlockPos();
        this.str = buf.readTextComponent();
        this.narrator = buf.readBoolean();
    }

    public UpdateServerSpeakerBlockPacket(BlockPos pos, String str, boolean narrator) {
        this.pos = pos;
        this.str = new StringTextComponent(str);
        this.narrator = narrator;
    }

    public static void buffer(UpdateServerSpeakerBlockPacket message, PacketBuffer buf) {

        buf.writeBlockPos(message.pos);
        buf.writeTextComponent(message.str);
        buf.writeBoolean(message.narrator);
    }

    public static void handler(UpdateServerSpeakerBlockPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = ctx.get().getSender().world;

        ctx.get().enqueueWork(() -> {
            if (world != null) {
                TileEntity tileentity = world.getTileEntity(message.pos);
                if (tileentity instanceof SpeakerBlockTile) {
                    SpeakerBlockTile speaker = (SpeakerBlockTile) tileentity;
                    speaker.message = message.str.getString();
                    speaker.narrator = message.narrator;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}