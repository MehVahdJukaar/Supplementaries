package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal {

    protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract int getTemper();

    @Shadow
    public abstract int getMaxTemper();

    @Shadow
    public abstract int modifyTemper(int addedTemper);

    @Shadow
    protected abstract void eating();

    @Shadow protected int temper;

    @Shadow public abstract boolean isTamed();

    @Inject(method = "addBehaviourGoals", at = @At("HEAD"))
    public void addSugarCube(CallbackInfo ci) {
        if (RegistryConfigs.SUGAR_CUBE_ENABLED.get()) {
            this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(ModRegistry.SUGAR_CUBE.get()), false));
        }
    }

    @Inject(method = "handleEating", at = @At("HEAD"), cancellable = true)
    public void eatSugarCube(Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModRegistry.SUGAR_CUBE.get().asItem())) {
            int duration = CommonConfigs.Blocks.SUGAR_BLOCK_HORSE_SPEED_DURATION.get();
            boolean eat = false;
            float healing = 1.0F;
            int ageIncrement = 30;
            int temper = 5;


            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(healing);
                eat = true;
            }

            if (this.isBaby()) {
                this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
                if (!this.level.isClientSide) {
                    this.ageUp(ageIncrement);
                }
                eat = true;

            }

            if ((eat || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
                eat = true;
                if (!this.level.isClientSide) {
                    this.modifyTemper(temper);
                }
            }
            if (duration != 0) {
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * duration));
                eat = true;
            }

            if (eat) {
                this.eating();
                this.gameEvent(GameEvent.EAT);
                cir.setReturnValue(true);
            }
        }
    }
}
