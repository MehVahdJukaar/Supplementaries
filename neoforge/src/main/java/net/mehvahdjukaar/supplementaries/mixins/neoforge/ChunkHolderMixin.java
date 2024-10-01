package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.neoforge.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundLevelChunkPacketData$BlockEntityInfo")
public abstract class ChunkHolderMixin {

    @Inject(method = "create(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData$BlockEntityInfo;",
            at = @At("HEAD"))
    private static void sendBlockEntityCaps(BlockEntity te, CallbackInfoReturnable<?> cir) {

        if (te != null && te.getLevel() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            BlockPos pos = te.getBlockPos();

            server.tell(new TickTask(server.getTickCount(), () -> {
                var cap = te.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
                if (cap.isPresent()) {
                    cap.ifPresent(c -> {
                        ServerChunkCache chunkSource = serverLevel.getChunkSource();
                        chunkSource.chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p ->
                                NetworkHelper.sendToClientPlayer(p,
                                        new ClientBoundSyncAntiqueInk(pos, c.hasAntiqueInk())));
                    });
                }
            }));
        }
    }
}
