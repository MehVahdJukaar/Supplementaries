package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements IQuiverEntity {


    protected LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }


    @Shadow
    @Override
    public abstract InteractionHand getUsedItemHand();


    @ModifyReturnValue(method = "hasEnoughImpulseToStartSprinting", at = @At("RETURN"))
    private boolean hasEnoughImpulseToStartSprinting(boolean oldValue) {
        if (oldValue && this.hasEffect(ModRegistry.OVERENCUMBERED.get())) {
            return false;
        }
        return oldValue;
    }

    //hack. this will be ugly. Prevents quiver from slowing down

    @Inject(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    shift = At.Shift.BEFORE),
            require = 1)
    private void cancelQuiverSlow(CallbackInfo ci, @Share("usingQuiver") LocalBooleanRef usingQuiver) {
        usingQuiver.set(true);
    }

    @Inject(method = "isUsingItem",
            at = @At("HEAD"), cancellable = true)
    private void isUsingItem(CallbackInfoReturnable<Boolean> cir,@Share("usingQuiver") LocalBooleanRef usingQuiver) {
        if (usingQuiver.get() && this.getUseItem().getItem() == ModRegistry.QUIVER_ITEM.get() && CommonConfigs.Tools.QUIVER_PREVENTS_SLOWS.get()) {
            cir.setReturnValue(false);
        }
    }
    @Unique
    private ItemStack supplementaries$quiver = ItemStack.EMPTY;

    //this isn't optimal but still better than checking every render tick the whole inventory
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
                    shift = At.Shift.AFTER)
    )
    private void checkIfHasQuiver(CallbackInfo ci) {
        supplementaries$quiver = QuiverItem.getQuiver(this);
    }

    @Override
    public ItemStack supplementaries$getQuiver() {
        return supplementaries$quiver;
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
        this.supplementaries$quiver = quiver;
    }
}