package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {

    @Invoker("onItemUse")
    InteractionResult invokeOnItemUse(UseOnContext arg, Function<UseOnContext, InteractionResult> callback);
}
