package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CelebrateVillagersSurvivedRaid.class)
public class CelebrateVillagersSurvivedRaidMixin {

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = {
            @At("TAIL")
    })
    protected void tick(ServerLevel level, Villager owner, long gameTime, CallbackInfo ci) {
        RandomSource randomSource = owner.getRandom();
        if (randomSource.nextInt(200) == 0) {
            if (!CommonConfigs.Tools.POPPER_ENABLED.get()) return;

            Vec3 viewVector = owner.getLookAngle();
            Vec3 spawnPos = owner.getEyePosition().add(viewVector.scale(0.2)).add(0d, -0.25, 0d);

            float degInc = 30;
            viewVector = owner.calculateViewVector(owner.getXRot() +
                    randomSource.nextFloat() * degInc, owner.getYRot()).normalize();
            ClientBoundParticlePacket packet = new ClientBoundParticlePacket(spawnPos, ClientBoundParticlePacket.Kind.CONFETTI, viewVector);

            if (!level.isClientSide) {
                NetworkHelper.sendToAllClientPlayersTrackingEntity(owner, packet);

                level.gameEvent(owner, GameEvent.EXPLODE, owner.position());
            }
        }

    }
}
