package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.supplementaries.common.entities.NavalRaidSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Raid.class)
public abstract class RaidMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    public abstract int getGroupsSpawned();

    @ModifyExpressionValue(method = "findRandomSpawnPos", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/SpawnPlacementType;isSpawnPositionOk(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z"))
    private boolean supp$acceptWaterSpawnPos(boolean original, @Local BlockPos.MutableBlockPos pos) {
        return original || NavalRaidSpawner.acceptsWaterSpawnPos(level, pos);
    }

    @Inject(method = "spawnGroup", at = @At("HEAD"))
    private void supp$checkNavalWave(BlockPos pos, CallbackInfo ci,
                                     @Share("naval") LocalBooleanRef naval,
                                     @Share("spawned") LocalRef<List<Raider>> spawned) {
        naval.set(NavalRaidSpawner.isNavalWave(level, pos));
        spawned.set(new ArrayList<>());
    }

    @WrapOperation(method = "spawnGroup", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    private Entity supp$swapRosterRaiderType(EntityType<?> type, Level level, Operation<Entity> original,
                                             @Share("naval") LocalBooleanRef naval) {
        if (naval.get()) {
            type = NavalRaidSpawner.navalRaiderType(type);
        }
        return original.call(type, level);
    }

    @WrapOperation(method = "spawnGroup", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/raid/Raid;joinRaid(ILnet/minecraft/world/entity/raid/Raider;Lnet/minecraft/core/BlockPos;Z)V"))
    private void supp$collectNavalRaider(Raid raid, int wave, Raider raider, BlockPos pos, boolean recruited,
                                         Operation<Void> original,
                                         @Share("naval") LocalBooleanRef naval,
                                         @Share("spawned") LocalRef<List<Raider>> spawned) {
        original.call(raid, wave, raider, pos, recruited);
        if (naval.get()) {
            spawned.get().add(raider);
        }
    }

    @Inject(method = "spawnGroup", at = @At("TAIL"))
    private void supp$boardBoats(BlockPos pos, CallbackInfo ci,
                                 @Share("naval") LocalBooleanRef naval,
                                 @Share("spawned") LocalRef<List<Raider>> spawned) {
        if (naval.get()) {
            NavalRaidSpawner.boardBoats(level, (Raid) (Object) this, this.getGroupsSpawned(), pos, spawned.get());
        }
    }
}
