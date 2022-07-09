package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.supplementaries.common.items.SlingshotItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SlingshotItem.class)
public abstract class SelfSlingshotMixin  extends Item {

    public SelfSlingshotMixin(Properties arg) {
        super(arg);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(
                Enchantments.QUICK_CHARGE, Enchantments.MULTISHOT, Enchantments.LOYALTY).contains(enchantment);
    }
}
