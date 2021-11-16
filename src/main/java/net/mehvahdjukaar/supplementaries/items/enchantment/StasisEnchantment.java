package net.mehvahdjukaar.supplementaries.items.enchantment;

import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.items.SlingshotItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class StasisEnchantment extends Enchantment {

    private final boolean enabled;

    public StasisEnchantment(Rarity rarity, EnchantmentType type, EquipmentSlotType... slotTypes) {
        super(rarity, type, slotTypes);
        enabled = RegistryConfigs.reg.SLINGSHOT_ENABLED.get();
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

    public boolean isTradeable() {
        return enabled;
    }

    public boolean isDiscoverable() {
        return enabled;
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
        return stack.getItem() instanceof SlingshotItem;
    }
}
