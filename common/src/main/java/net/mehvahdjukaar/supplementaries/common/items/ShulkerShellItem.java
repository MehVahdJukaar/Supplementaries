package net.mehvahdjukaar.supplementaries.common.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ShulkerShellItem extends ArmorItem {
    public ShulkerShellItem(Properties properties) {
        super(new SkulkerShellArmorMaterial(), EquipmentSlot.HEAD, properties);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 64;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    private static class SkulkerShellArmorMaterial implements ArmorMaterial {
        private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};

        @Override
        public int getDurabilityForSlot(EquipmentSlot slotType) {
            return HEALTH_PER_SLOT[slotType.getIndex()] * 10;
        }

        @Override
        public int getDefenseForSlot(EquipmentSlot p_200902_1_) {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.SHULKER_CLOSE;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "shulker_shell";
        }

        @Override
        public float getToughness() {
            return 1;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.2f;
        }
    }

}
