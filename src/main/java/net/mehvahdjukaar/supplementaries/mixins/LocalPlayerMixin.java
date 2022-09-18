package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements IQuiverEntity {

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Inject(method = "hasEnoughImpulseToStartSprinting", at = @At("RETURN"), cancellable = true)
    private void hasEnoughImpulseToStartSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.hasEffect(ModRegistry.OVERENCUMBERED.get())) {
            cir.setReturnValue(false);
        }
    }

    //hack. this will be ugly

    @Inject(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    shift = At.Shift.BEFORE),
            require = 3)
    private void cancelQuiverSlow(CallbackInfo ci) {
        this.cancelUsingQuiver = true;
    }

    @Inject(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    shift = At.Shift.AFTER),
            require = 3)
    private void reset(CallbackInfo ci) {
        this.cancelUsingQuiver = false;
    }

    @Inject(method = "isUsingItem",
            at = @At("HEAD"), cancellable = true)
    private void isUsingItem(CallbackInfoReturnable<Boolean> cir) {
        if (cancelUsingQuiver && this.getUseItem().getItem() == ModRegistry.QUIVER_ITEM.get() && ServerConfigs.item.QUIVER_PREVENTS_SLOWS.get()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean cancelUsingQuiver = false;

    @Unique
    private ItemStack quiver = ItemStack.EMPTY;

    //this isn't optimal but still better than checking every render tick the whole inventory
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
                    shift = At.Shift.AFTER)
    )
    private void checkIfHasQuiver(CallbackInfo ci) {
        quiver = QuiverItem.getQuiver(this);
    }

    @Override
    public ItemStack getQuiver() {
        return quiver;
    }

    @Override
    public void setQuiver(ItemStack quiver) {
        this.quiver = quiver;
    }
}
