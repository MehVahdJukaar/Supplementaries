package net.mehvahdjukaar.supplementaries.network;

import mezz.jei.render.IngredientListBatchRenderer;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundSyncAntiqueInk implements NetworkHandler.Message {
    private final BlockPos pos;
    private final boolean ink;

    public ClientBoundSyncAntiqueInk(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.ink = buffer.readBoolean();
    }

    public ClientBoundSyncAntiqueInk(BlockPos pos, boolean ink) {
        this.pos = pos;
        this.ink = ink;
    }

    public static void buffer(ClientBoundSyncAntiqueInk message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeBoolean(message.ink);
    }

    public static void handler(ClientBoundSyncAntiqueInk message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientReceivers.handleSyncAntiqueInkPacket(message);
            }
        });
        context.setPacketHandled(true);
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean getInk() {
        return ink;
    }
}