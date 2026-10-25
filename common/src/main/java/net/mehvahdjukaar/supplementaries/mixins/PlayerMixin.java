package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.CannonFlight;
import net.mehvahdjukaar.supplementaries.common.entities.ICannonRider;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


//i already know so many mods will break this cannon stuff
@Mixin(Player.class)
public class PlayerMixin implements IQuiverEntity, ICannonRider {

    @Unique
    @Nullable
    private BlockPos supplementaries$cannonPos = null;

    @Unique
    private final CannonFlight supplementaries$cannonFlight = new CannonFlight((Player) (Object) this);

    @Override
    public ItemStack supplementaries$getQuiver() {
        return ItemStack.EMPTY;
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
    }

    @Override
    public @Nullable BlockPos supplementaries$getCannonPos() {
        return supplementaries$cannonPos;
    }

    @Override
    public void supplementaries$setCannonPos(@Nullable BlockPos pos) {
        this.supplementaries$cannonPos = pos;
    }

    @Override
    public CannonFlight supplementaries$getCannonFlight() {
        return supplementaries$cannonFlight;
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 suppl$dontSteerMidFlight(Vec3 travelVector) {
        return supplementaries$cannonFlight.isFlying() ? Vec3.ZERO : travelVector;
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void suppl$dragMidFlight(Vec3 travelVector, CallbackInfo ci) {
        if (supplementaries$cannonFlight.isFlying()) supplementaries$cannonFlight.applyDrag();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void suppl$holdInCannon(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (supplementaries$cannonFlight.isFlying()) supplementaries$cannonFlight.tick();
        CannonBlockTile cannon = CannonBlockTile.riddenBy(player);
        if (cannon == null) return;
        Vec3 seat = cannon.getSeatPosition(1);
        if (player.level().isClientSide) {
            player.setPos(seat);
            player.xo = seat.x;
            player.yo = seat.y;
            player.zo = seat.z;
        } else {
            cannon.keepRiderSeated(player);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;

        float barrelYaw = EntityAngles.fromQuaternion(cannon.getWorldOrientation(1)).yaw();
        player.yBodyRot = barrelYaw;
        player.yBodyRotO = barrelYaw;
    }

    @Inject(method = "updatePlayerPose", at = @At("TAIL"))
    private void suppl$keepRiderPoseSmall(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (CannonBlockTile.riddenBy(player) != null) {
            player.setPose(Pose.SWIMMING);
        } else if (supplementaries$cannonFlight.isFlying()) {
            player.setPose(Pose.FALL_FLYING); //same hitbox as elytra
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void suppl$landSafelyFromCannon(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!supplementaries$cannonFlight.isFlying()) return;
        supplementaries$cannonFlight.playLandingSound(fallDistance);
        cir.setReturnValue(false);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void suppl$ejectWhenHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!cir.getReturnValueZ() || player.level().isClientSide) return;
        CannonBlockTile cannon = CannonBlockTile.riddenBy(player);
        if (cannon != null) cannon.dismount();
    }
}
