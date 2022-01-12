package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;


@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundLevelChunkPacketData$BlockEntityInfo")
public abstract class ChunkHolderMixin {



    @Inject(method = "create(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData$BlockEntityInfo;",
            at = @At("HEAD"))
    private static void sendBlockEntityCaps(BlockEntity te, CallbackInfoReturnable cir) {

        if(te != null && te.getLevel() instanceof ServerLevel serverLevel) {
            var cap = te.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
            if (cap.isPresent()) {
                MinecraftServer server = serverLevel.getServer();
                BlockPos pos = te.getBlockPos();

                server.tell(new TickTask(server.getTickCount() , () -> {
                    var c = cap.orElse(null);
                    if (c != null) {
                        ServerChunkCache chunkSource = serverLevel.getChunkSource();
                        chunkSource.chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p ->
                                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with((() -> p)),
                                        new ClientBoundSyncAntiqueInk(pos, c.hasAntiqueInk())));
                    }
                }));
            }
        }


    }
}
