package net.mehvahdjukaar.supplementaries.common.effects;

import net.mehvahdjukaar.supplementaries.common.items.BubbleBlower;
import net.mehvahdjukaar.supplementaries.common.items.SlingshotItem;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class StasisEnchantment extends Enchantment {

    public static final boolean ENABLED = RegistryConfigs.Reg.STASIS_ENABLED.get() &&
            (RegistryConfigs.Reg.SLINGSHOT_ENABLED.get() || RegistryConfigs.Reg.BUBBLE_BLOWER_ENABLED.get());

    public StasisEnchantment() {
        super(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.CROSSBOW,new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + level * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return 40;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return ENABLED;
    }

    @Override
    public boolean isDiscoverable() {
        return ENABLED;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ENABLED;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.MULTISHOT;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        Item i = stack.getItem();
        return i instanceof SlingshotItem || i instanceof BubbleBlower;
    }
}
