package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements IQuiverPlayer {


    protected LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "hasEnoughImpulseToStartSprinting", at = @At("RETURN"), cancellable = true)
    private void suppl$PreventSprintingWithOverencumbered(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.hasEffect(ModRegistry.OVERENCUMBERED.get())) {
            cir.setReturnValue(false);
        }
    }

    // prevents quiver slow
    @ModifyExpressionValue(method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean suppl$preventQuiverSlow(boolean original) {
        if (this.getUseItem().getItem() == ModRegistry.QUIVER_ITEM.get() && CommonConfigs.Tools.QUIVER_PREVENTS_SLOWS.get()) {
            return false;
        }
        return original;
    }

    // cached quiver
    @Unique
    private ItemStack supplementaries$quiverForRenderer = ItemStack.EMPTY;
    @Unique
    private SlotReference supplementaries$quiverSlotForHUD = SlotReference.EMPTY;

    // this isn't optimal but still better than checking every render tick the whole inventory
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
                    shift = At.Shift.AFTER)
    )
    private void suppl$checkIfHasQuiver(CallbackInfo ci) {
       supplementaries$quiverSlotForHUD = QuiverItem.findActiveQuiverSlot(this);
       supplementaries$quiverForRenderer = supplementaries$quiverSlotForHUD.get(this);
    }

    @Override
    public ItemStack supplementaries$getQuiver() {
        return supplementaries$quiverForRenderer;
    }

    @Override
    public SlotReference supplementaries$getQuiverSlot(){
        return supplementaries$quiverSlotForHUD;
    };


    @Override
    public void supplementaries$setQuiver(@NotNull ItemStack quiver) {
        this.supplementaries$quiverForRenderer = quiver;
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"))
    public boolean suppl$preventMovementWhileOperatingCannon(Input instance, boolean bl, float f) {
        if (CannonController.isActive()) {
            CannonController.onInputUpdate(instance);
            return false;
        }
        return true;
    }
}