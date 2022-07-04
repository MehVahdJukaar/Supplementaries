package net.mehvahdjukaar.supplementaries.mixins.accessors;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DispenserBlock.class)
public interface DispenserAccessor {

    @Invoker
    DispenseItemBehavior invokeGetDispenseMethod(ItemStack pStack);

}