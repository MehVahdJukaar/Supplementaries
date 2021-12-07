package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundSpawnBlockParticlePacket implements NetworkHandler.Message {
    private final BlockPos pos;
    private final int id;

    public ClientBoundSpawnBlockParticlePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.id = buffer.readInt();
    }

    public ClientBoundSpawnBlockParticlePacket(BlockPos pos, int id) {
        this.pos = pos;
        this.id = id;
    }

    public static void buffer(ClientBoundSpawnBlockParticlePacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeInt(message.id);
    }

    public static void handler(ClientBoundSpawnBlockParticlePacket message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                //assigns data to client
                // Level world = Objects.requireNonNull(context.getSender()).level;
                if (message.id == 0) {
                    spawnParticles(message.pos);

                }
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnParticles(BlockPos pos){
        ParticleUtil.spawnParticlesOnBlockFaces(Minecraft.getInstance().level, pos, ModRegistry.SUDS_PARTICLE.get(),
                UniformInt.of(2, 4), 0.01f, true);
    }
}