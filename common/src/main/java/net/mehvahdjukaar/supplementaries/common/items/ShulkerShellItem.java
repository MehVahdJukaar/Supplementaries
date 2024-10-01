package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

//forge only. don't bother for fabric
public class ShulkerShellItem extends ArmorItem {

    public static final RegSupplier<ArmorMaterial> TURTLE_MATERIAL = RegHelper.register(Supplementaries.res("shulker_shell"),
            () -> new ArmorMaterial(Map.of(
                    ArmorItem.Type.BOOTS, 0,
                    ArmorItem.Type.LEGGINGS, 0,
                    ArmorItem.Type.CHESTPLATE, 0,
                    ArmorItem.Type.HELMET, 0,
                    ArmorItem.Type.BODY, 0),
                    0,
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHULKER_CLOSE),
                    () -> Ingredient.EMPTY,
                    List.of(new ArmorMaterial.Layer(Supplementaries.res("shulker_shell"))),
                    1,
                    0.2f
            ),
            Registries.ARMOR_MATERIAL);


    public ShulkerShellItem(Properties properties) {
        super(TURTLE_MATERIAL.getHolder(), Type.HELMET, properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

}
