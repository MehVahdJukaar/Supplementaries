package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.api.IExtendedItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

//makes any item potentially placeable
@Mixin(Item.class)
public abstract class ItemMixin implements IExtendedItem {

    @Unique
    @Nullable
    private IAdditionalPlacement additionalBehavior;

    @Shadow
    @Final
    @Nullable
    private FoodProperties foodProperties;

    //delegates stuff to internal blockItem
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        IAdditionalPlacement behavior = this.getAdditionalBehavior();
        if (behavior != null) {
            var result = behavior.overrideUseOn(pContext, foodProperties);
            if (result.consumesAction()) cir.setReturnValue(result);
        }
    }

    //delegates stuff to internal blockItem
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced, CallbackInfo ci) {
        IAdditionalPlacement behavior = this.getAdditionalBehavior();
        if (behavior != null) {
            behavior.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }

    @Nullable
    public IAdditionalPlacement getAdditionalBehavior() {
        return this.additionalBehavior;
    }

    @Override
    public void addAdditionalBehavior(IAdditionalPlacement placementOverride) {
        this.additionalBehavior = placementOverride;
    }
}
