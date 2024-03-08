package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.entities.CannonCamera;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets entities get sent to the client even though they're not in range of the player
 */
@Mixin(value = ChunkMap.TrackedEntity.class, priority = 1100)
public abstract class TrackedEntityMixin {
    @Shadow
    @Final
    Entity entity;

    @WrapOperation(method = "updatePlayer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;broadcastToPlayer(Lnet/minecraft/server/level/ServerPlayer;)Z"))
    public boolean securitycraft$modifyFlag(Entity instance, ServerPlayer player, Operation<Boolean> original,
											@Local(ordinal = 0) double viewDistance) {
		boolean shouldSend = false;
		if (CannonCamera.isBeingViewedBy(player)) {
			Vec3 relativePosToCamera = player.getCamera().position().subtract(entity.position());

			if (relativePosToCamera.x >= -viewDistance && relativePosToCamera.x <= viewDistance && relativePosToCamera.z >= -viewDistance && relativePosToCamera.z <= viewDistance) {
				shouldSend = true;
			}
		}
		return original.call(instance, player) || entity instanceof CannonCamera || shouldSend;
	}
}
