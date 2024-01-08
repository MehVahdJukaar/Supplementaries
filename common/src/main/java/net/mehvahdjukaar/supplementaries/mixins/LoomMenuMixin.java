package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin extends AbstractContainerMenu {

    protected LoomMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @WrapOperation(method ="quickMoveStack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                    ordinal = 0))
    public Item getItem(ItemStack instance, Operation<Item> original) {
        if(instance.getItem() instanceof FlagItem fi){
            return BannerBlock.byColor(fi.getColor()).asItem();
        }
        return original.call(instance);
    }
}
