package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.commands.DebugRenderersCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(DebugPackets.class)
public abstract class DebugPacketsMixin {

    @Shadow
    private static void sendPacketToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
    }

    @Inject(method = "sendPathFindingPacket", at = @At("HEAD"))
    private static void supp$sendPathfindingDebug(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint, CallbackInfo ci) {
        if (path != null && DebugRenderersCommand.debugNavigation && level instanceof ServerLevel sl) {
            Path.DebugData debugData = path.debugData();
            if(debugData == null){
                List<Node> close = new ArrayList<>();
                List<Node> open = new ArrayList<>();
                Set<Target> targets = new HashSet<>();
                for (int i = 0; i<path.getNodeCount(); i ++){
                    Node node = path.getNode(i);
                     if(node.closed){
                        close.add(node);
                    }else open.add(node);

                }
                BlockPos pos = path.getTarget();

                Target t = mob.getNavigation().getNodeEvaluator().getTarget(pos.getX(), pos.getY(), pos.getZ());
                targets.add(t);
                path.setDebug(open.toArray(new Node[0]), close.toArray(new Node[0]), targets);
                debugData = path.debugData();
            }
            if (debugData != null && !debugData.targetNodes().isEmpty()) {
                sendPacketToAllPlayers(sl, new PathfindingDebugPayload(mob.getId(), path, maxDistanceToWaypoint));
            }
        }
    }

    @Inject(method = "sendNeighborsUpdatePacket", at = @At("HEAD"))
    private static void supp$neighborUpdatesDebug(Level level, BlockPos pos, CallbackInfo ci) {
        if (DebugRenderersCommand.debugNeighbors && level instanceof ServerLevel sl) {
            sendPacketToAllPlayers(sl, new NeighborUpdatesDebugPayload(level.getGameTime(), pos));
        }
    }

    @Inject(method = "sendGoalSelector", at = @At("HEAD"))
    private static void supp$goalDebug(Level level, Mob mob, GoalSelector goalSelector, CallbackInfo ci) {
        if (DebugRenderersCommand.debugGoals && level instanceof ServerLevel sl) {
            List<GoalDebugPayload.DebugGoal> goals = new ArrayList<>();
            for (var g : goalSelector.getAvailableGoals()) {
                goals.add(new GoalDebugPayload.DebugGoal(g.getPriority(), g.isRunning(), g.getGoal().toString()));
            }
            sendPacketToAllPlayers(sl, new GoalDebugPayload(mob.getId(), mob.blockPosition(), goals));
        }
    }

}
