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

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
            at = @At("TAIL"))
    protected void supp$popConfetti(ServerLevel level, Villager owner, long gameTime, CallbackInfo ci) {
        RandomSource random = owner.getRandom();
        if (random.nextInt(200) != 0) return;
        if (!CommonConfigs.Tools.POPPER_ENABLED.get()) return;

        float upwardsTilt = 30;
        Vec3 dir = owner.calculateViewVector(owner.getXRot() - random.nextFloat() * upwardsTilt,
                owner.getYRot()).normalize();
        Vec3 spawnPos = owner.getEyePosition().add(dir.scale(0.2)).add(0, -0.25, 0);

        NetworkHelper.sendToAllClientPlayersTrackingEntity(owner,
                new ClientBoundParticlePacket(spawnPos, ClientBoundParticlePacket.Kind.CONFETTI, dir));
        level.gameEvent(owner, GameEvent.EXPLODE, owner.position());
    }
}
