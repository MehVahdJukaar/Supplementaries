package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.items.ShulkerShellItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Items.class)
public abstract class ItemsMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW",
            target = "(Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;"

    ),
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=shulker_shell"
                    )
            ))
    private static Item supp$overrideShulkerShell(Item.Properties properties, Operation<Item> original) {
        return new ShulkerShellItem(properties);
    }
}
