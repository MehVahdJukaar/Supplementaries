package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity {
    protected AbstractArrowMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract ItemStack getPickupItem();

    @Shadow protected boolean inGround;

    @Shadow public abstract boolean isNoPhysics();

    @Shadow public int shakeTime;

    @Shadow public AbstractArrow.Pickup pickup;

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    public void onPlayerTouch(Player player, CallbackInfo ci){
        if(!this.level().isClientSide) {
            if ((this.inGround || this.isNoPhysics()) && this.pickup == AbstractArrow.Pickup.ALLOWED && this.shakeTime <= 0 &&
                    ServerEvents.onArrowPickup((AbstractArrow) (Object) this, player, this::getPickupItem)) {
                ci.cancel();
            }
        }
    }
}
