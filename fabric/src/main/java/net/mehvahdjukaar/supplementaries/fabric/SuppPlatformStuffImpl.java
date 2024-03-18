package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.BoolConfigValue;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.mixins.fabric.BiomeAccessor;
import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SuppPlatformStuffImpl {


    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).getType();
    }

    @Nullable
    public static <T> T getForgeCap(Object object, Class<T> capClass) {
        return null;
    }

    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        return null;
    }

    public static boolean isEndermanMask(EnderMan enderman, Player player, ItemStack itemstack) {
        return itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem() ||
                EnchantmentHelper.getEnchantments(itemstack)
                        .containsKey(CompatObjects.END_VEIL.get());
    }

    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        return 6000;
    }

    public static void onItemPickup(Player player, ItemEntity itemEntity, ItemStack copy) {
    }

    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        return c;
    }

    public static float getDownfall(Biome biome) {
        return ((BiomeAccessor) (Object) biome).getClimateSettings().downfall();
    }

    public static void disableAMWarn() {
        ((BoolConfigValue) ClientConfigs.General.NO_AMENDMENTS_WARN).set(true);
        ((FabricConfigSpec) ClientConfigs.SPEC).saveConfig();
    }

    public static void disableOFWarn() {
        ((BoolConfigValue) ClientConfigs.General.NO_OPTIFINE_WARN).set(true);
        ((FabricConfigSpec) ClientConfigs.SPEC).saveConfig();
    }

}
