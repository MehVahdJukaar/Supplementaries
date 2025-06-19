package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.hauntedharvest.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {

    @Inject(method = "broadcastBlockEntity", at = @At("TAIL"))
    private void syncBedCap(List<ServerPlayer> list, Level arg, BlockPos pos, CallbackInfo ci, @Local BlockEntity te) {
        var cap = te.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
        if (cap.isPresent()) {
            var c = cap.orElseThrow(() -> new IllegalStateException("Antique text capability was null. How?"));
            list.forEach(p -> NetworkHandler.CHANNEL.sendToClientPlayer(p, new ClientBoundSyncAntiqueInk(pos, c.hasAntiqueInk())));
        }
    }
}
