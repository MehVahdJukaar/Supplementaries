package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DispenserBlock.class)
public interface IDispenserAccessor {

    @Invoker("getDispenseMethod")
    DispenseItemBehavior invokeGetDispenseMethod(Level level, ItemStack itemStack);

}
