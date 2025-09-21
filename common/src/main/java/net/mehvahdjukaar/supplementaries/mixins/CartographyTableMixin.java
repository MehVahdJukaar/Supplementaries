package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.WeatheredHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMixin extends AbstractContainerMenu {

    @Shadow
    @Final
    private ResultContainer resultContainer;

    protected CartographyTableMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "quickMoveStack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
                    ordinal = 0), cancellable = true)
    public void supp$getItem(Player player, int index, CallbackInfoReturnable<ItemStack> cir,
                             @Local(ordinal = 0) ItemStack itemStack, @Local Slot slot, @Local(ordinal = 1) ItemStack itemStack2) {

        if (itemStack2.is(ModRegistry.ANTIQUE_INK.get())) {
            if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemStack2.getCount() == itemStack.getCount()) {
                cir.setReturnValue(ItemStack.EMPTY);
            }

            slot.onTake(player, itemStack2);
            this.broadcastChanges();
            cir.setReturnValue( itemStack);

        }

    }

    @Inject(method = "method_17382",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
                    ordinal = 0), cancellable = true)
    public void supp$getItem(ItemStack mapStack, ItemStack firstSlotStack, ItemStack resultSlotStack, Level level,
                             BlockPos blockPos, CallbackInfo ci, @Local MapItemSavedData mapItemSavedData) {
        if (firstSlotStack.is(ModRegistry.ANTIQUE_INK.get()) && !mapItemSavedData.locked && !AntiqueInkItem.hasAntiqueInk(mapStack)) {

            ItemStack newMap = mapStack.copyWithCount(1);
            WeatheredHandler.setAntique(level, newMap, true, false);
            AntiqueInkItem.setAntiqueInk(newMap, true);

            this.resultContainer.setItem(2, newMap);
            this.broadcastChanges();
            ci.cancel();
        }
    }
}
