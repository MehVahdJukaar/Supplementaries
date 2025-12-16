package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.items.SlingshotItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SlingshotItem.class)
public abstract class SelfSlingshotMixin extends Item {

    public SelfSlingshotMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.MULTISHOT) || enchantment.is(Enchantments.QUICK_CHARGE)) return true;
        return super.supportsEnchantment(stack, enchantment);
    }

}
