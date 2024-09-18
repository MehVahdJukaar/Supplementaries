package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {


    @Shadow public abstract FriendlyByteBuf writeItemStack(ItemStack par1, boolean par2);

    @Inject(method = "writeItemStack", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void supp$sendCapsFromCreative(ItemStack stack, boolean useShareTag, CallbackInfoReturnable<FriendlyByteBuf> cir) {
        if (!useShareTag && stack.getItem() instanceof SelectableContainerItem<?>) {
            cir.setReturnValue(this.writeItemStack(stack, true)); //thanks forge
        }
    }
}
