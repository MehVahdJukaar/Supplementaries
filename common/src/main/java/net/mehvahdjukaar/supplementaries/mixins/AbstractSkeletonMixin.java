package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster {

    protected AbstractSkeletonMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    public void finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (this.getType() == EntityType.SKELETON || this.getType() == EntityType.STRAY && CommonConfigs.Tools.QUIVER_ENABLED.get()) {
            float chance = CommonConfigs.Tools.QUIVER_SKELETON_SPAWN.get();
            if (CommonConfigs.Tools.QUIVER_SKELETON_SPAWN_LOCAL_DIFFICULTY)
                chance *= difficulty.getSpecialMultiplier();
            if (random.nextFloat() < chance) {
                ((IQuiverEntity) this).supplementaries$setQuiver(
                        RandomArrowFunction.createRandomQuiver(level.getRandom(), difficulty.getSpecialMultiplier()));
            }
        }
    }

    // correct arrow is already handled by item mixin. Here we just remove from quiver
    @Inject(method = "performRangedAttack", at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/entity/monster/AbstractSkeleton;getArrow(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/projectile/AbstractArrow;",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void consumeQuiverArrow(LivingEntity target, float velocity, CallbackInfo ci, ItemStack arrow) {
        if (this instanceof IQuiverEntity quiverEntity) {
            var quiver = quiverEntity.supplementaries$getQuiver();
            //ignore offhand as it has priority over quiver
            if (!quiver.isEmpty() && this.getItemInHand(InteractionHand.OFF_HAND).getItem() != arrow.getItem()) {
                var data = QuiverItem.getQuiverData(quiver);
                if (data != null) data.consumeArrow();
            }
        }
    }


}