package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal {

    protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addBehaviourGoals", at = @At("HEAD"))
    public void addSugarCube(CallbackInfo ci) {
        if (CommonConfigs.Building.SUGAR_CUBE_ENABLED.get()) {
            this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(ModRegistry.SUGAR_CUBE.get()), false));
        }
    }

    //TODO: test
    @Inject(method = "handleEating", at = @At(value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;getHealth()F"))
    private void eatSugarCube(Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir,
                                    @Local(ordinal = 0) LocalBooleanRef eat,
                                    @Local(ordinal = 0) LocalFloatRef healing,
                                    @Local(ordinal = 0) LocalIntRef ageIncrement,
                                    @Local(ordinal = 1) LocalIntRef newTemper) {
        if (stack.is(ModRegistry.SUGAR_CUBE.get().asItem())) {
            healing.set(1.0F);
            ageIncrement.set(30);
            newTemper.set(5);

            int duration = CommonConfigs.Building.SUGAR_BLOCK_HORSE_SPEED_DURATION.get();
            if (duration != 0) {
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * duration, 1));
                eat.set(true);
            }
        }
    }

}
