package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.common.world.explosion.GunpowderExplosion;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

//forge only. don't bother for fabric
public class ShulkerShellItem extends ArmorItem {

    public ShulkerShellItem(Properties properties) {
       super(new SkulkerShellArmorMaterial(), EquipmentSlot.HEAD, properties);
    }

public static class b extends Block{

    public b(Properties arg) {
        super(arg);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return super.shouldCheckWeakPower(state, level, pos, side);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return super.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return super.isSignalSource(state);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return super.getSignal(state, level, pos, direction);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return super.canConnectRedstone(state, level, pos, direction);
    }
}

    @Override
    public int getMaxStackSize(ItemStack stack) {
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
