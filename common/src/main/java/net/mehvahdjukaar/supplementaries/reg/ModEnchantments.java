package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.misc.HolderRef;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.Supplementaries.res;

public class ModEnchantments {

    //enchantment
    public static final HolderRef<Enchantment> STASIS_ENCHANTMENT = HolderRef.of(
            res("stasis"), Registries.ENCHANTMENT);
    public static final HolderRef<Enchantment> WHIMSY_ENCHANTMENT = HolderRef.of(
            res(ModConstants.WHIMSY_NAME), Registries.ENCHANTMENT);
    public static final HolderRef<Enchantment> CAROUSEL_ENCHANTMENT = HolderRef.of(
            res(ModConstants.CAROUSEL_NAME), Registries.ENCHANTMENT);

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
    public static final Supplier<DataComponentType<Unit>> RANDOMIZE_SELECTED_SLOT = RegHelper.register(
            res("randomize_selected_slot"),
            () -> DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC).build(),
            Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE
    );
    public static final Supplier<DataComponentType<Unit>> CYCLE_SELECTED_SLOT = RegHelper.register(
            res("cycle_selected_slot"),
            () -> DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC).build(),
            Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE
    );

    public static void init() {
    }

    //TBH we could have just use tags here instead with an empty stasis ench definition
}
