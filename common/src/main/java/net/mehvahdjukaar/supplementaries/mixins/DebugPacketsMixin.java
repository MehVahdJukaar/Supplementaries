package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.commands.DebugRenderersCommand;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundDebugPathfindingPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugPackets.class)
public abstract class DebugPacketsMixin {

    @Shadow
    private static void sendPacketToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
    }

    @Inject(method = "sendPathFindingPacket", at = @At("HEAD"))
    private static void supp$sendPathfindingDebug(Level level, Mob mob, Path path, float maxDistanceToWaypoint, CallbackInfo ci) {
        if (DebugRenderersCommand.debugNavigation && level instanceof ServerLevel sl) {
            if (path.debugData() != null && !path.debugData().targetNodes().isEmpty()) {
                sendPacketToAllPlayers(sl, new PathfindingDebugPayload(mob.getId(), path, maxDistanceToWaypoint));
            }
        }
    }

}
