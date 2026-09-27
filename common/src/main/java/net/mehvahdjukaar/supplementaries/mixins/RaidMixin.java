package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.entities.NavalRaidSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(Raid.class)
public abstract class RaidMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Map<Integer, Set<Raider>> groupRaiderMap;

    @Shadow
    public abstract int getGroupsSpawned();

    @Shadow
    public abstract BlockPos getCenter();

    //only the last 20 try call in tick(). If that fails too vanilla stop()s the raid
    @ModifyReturnValue(method = "findRandomSpawnPos", at = @At("RETURN"))
    private BlockPos supp$spawnOnWaterWhenNoLand(BlockPos original, int offsetMultiplier, int maxTry) {
        if (original != null || offsetMultiplier < 3 || maxTry <= 1) return original;
        return NavalRaidSpawner.findWaterSpawnPosWhenNoLand(level, getCenter());
    }

    //groupsSpawned is already bumped here so it matches the wave vanilla just spawned
    @Inject(method = "spawnGroup", at = @At("TAIL"))
    private void supp$spawnNavalDivision(BlockPos pos, CallbackInfo ci) {
        int wave = this.getGroupsSpawned();
        NavalRaidSpawner.spawnNavalDivision(level, (Raid) (Object) this, wave, pos, groupRaiderMap.get(wave));
    }
}
