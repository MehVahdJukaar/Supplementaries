package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.Supplementaries.res;

public class ModEnchantments {

    public static void init() {
    }

    //enchantment
    public static final HolderReference<Enchantment> STASIS_ENCHANTMENT = HolderReference.of(
            res("stasis"), Registries.ENCHANTMENT);


    public static final Supplier<DataComponentType<Unit>> PROJECTILE_NO_GRAVITY = RegHelper.register(
            res("projectile_no_gravity"),
            () -> DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC).build(),
            Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE
    );

    public static final Supplier<DataComponentType<Unit>> SPAWN_BUBBLE_BLOCK = RegHelper.register(
            res("spawn_bubble_block"),
            () -> DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC).build(),
            Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE
    );

    //TBH we could have just use tags here instead with an empty stasis ench definition
}
