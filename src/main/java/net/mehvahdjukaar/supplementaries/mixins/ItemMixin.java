package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.common.items.IPlaceableItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

@Mixin(Item.class)
public class ItemMixin implements IPlaceableItem {

    @Unique
    @Nullable
    private Block placeable;

    @Shadow
    @Final
    @Nullable
    private FoodProperties foodProperties;

    //delegates stuff to internal blockItem
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.placeable != null) {
            cir.setReturnValue(this.getPlacer().mimicUseOn(pContext, placeable, foodProperties));
        }
    }

    //delegates stuff to internal blockItem
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced, CallbackInfo ci) {
        if (this.placeable != null && ClientConfigs.cached.PLACEABLE_TOOLTIPS) {
            pTooltipComponents.add(new TranslatableComponent("message.supplementaries.placeable").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }

    @Nullable
    public Block getPlaceableBlock() {
        return placeable;
    }

    @Override
    public void makePlaceable(Block placeableState) {
        this.placeable = placeableState;

    }
}
