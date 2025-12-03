package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.misc.OptHolderRef;
import net.mehvahdjukaar.moonlight.api.misc.OptRegSupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class CompatObjects {

    //these can return null on get instead of throwing

    public static final OptRegSupplier<Block> WALL_LANTERN = builtin("amendments:wall_lantern", Registries.BLOCK);
    public static final OptRegSupplier<Block> DIRECTIONAL_CAKE = builtin("amendments:directional_cake", Registries.BLOCK);

    public static final OptRegSupplier<Block> COPPER_LANTERN = builtin("suppsquared:copper_lantern", Registries.BLOCK);
    public static final OptRegSupplier<Block> CHANDELIER = builtin("decorative_blocks:chandelier", Registries.BLOCK);

    public static final OptRegSupplier<Block> SOUL_CHANDELIER = builtin("decorative_blocks:soul_chandelier", Registries.BLOCK);

    public static final OptRegSupplier<Block> GLOW_CHANDELIER = builtin("muchmoremodcompat:glow_chandelier", Registries.BLOCK);

    public static final OptRegSupplier<Block> ENDER_CHANDELIER = builtin("decorative_blocks_abnormals:ender_chandelier", Registries.BLOCK);

    public static final OptRegSupplier<Block> SAPPY_MAPLE_LOG = builtin("autumnity:sappy_maple_log", Registries.BLOCK);

    public static final OptRegSupplier<Block> SAPPY_MAPLE_WOOD = builtin("autumnity:sappy_maple_wood", Registries.BLOCK);

    public static final OptRegSupplier<Block> TATER = builtin("quark:tiny_potato", Registries.BLOCK);

    public static final OptRegSupplier<Block> TOMATO_CROP = builtin("farmersdelight:tomatoes", Registries.BLOCK);


    public static final OptRegSupplier<ParticleType<?>> ENDER_FLAME = builtin("endergetic:ender_fire_flame", Registries.PARTICLE_TYPE);
    public static final OptRegSupplier<ParticleType<?>> GLOW_FLAME = builtin("infernalexp:glowstone_sparkle", Registries.PARTICLE_TYPE);
    public static final OptRegSupplier<ParticleType<?>> NETHER_BRASS_FLAME = builtin("architects_palette:green_flame", Registries.PARTICLE_TYPE);

    public static final OptRegSupplier<ParticleType<?>> SMALL_SOUL_FLAME = builtin("buzzier_bees:small_soul_fire_flame", Registries.PARTICLE_TYPE);
    public static final OptRegSupplier<ParticleType<?>> SMALL_CUPRIC_FLAME = builtin("caverns_and_chasms:small_cupric_fire_flame", Registries.PARTICLE_TYPE);
    public static final OptRegSupplier<ParticleType<?>> SMALL_END_FLAME = builtin("endergetic:small_ender_fire_flame", Registries.PARTICLE_TYPE);

    public static final OptRegSupplier<Block> SUGAR_WATER = builtin("the_bumblezone:sugar_water_block", Registries.BLOCK);

    public static final OptRegSupplier<Block> MILK_CAULDRON = builtin("rats:milk_cauldron", Registries.BLOCK);

    public static final OptRegSupplier<Item> TOME = builtin("quark:ancient_tome", Registries.ITEM);
    public static final OptRegSupplier<Item> GENE_BOOK = builtin("horse_colors:gene_book", Registries.ITEM);

    public static final OptRegSupplier<Item> BARBARIC_HELMET = builtin("goated:barbaric_helmet", Registries.ITEM);

    public static final OptRegSupplier<Item> ATLAS = builtin("map_atlases:atlas", Registries.ITEM);
    public static final OptRegSupplier<Item> DYE_BOTTLE = builtin("amendments:dye_bottle", Registries.ITEM);

    public static final OptRegSupplier<Block> RICH_SOIL = builtin("farmersdelight:rich_soil", Registries.BLOCK);
    public static final OptRegSupplier<Block> TOMATOES = builtin("farmersdelight:tomatoes", Registries.BLOCK);

    public static final OptRegSupplier<Block> RICH_SOUL_SOIL = builtin("nethers_delight:rich_soul_soil", Registries.BLOCK);

    public static final OptRegSupplier<ParticleType<?>> SHARPNEL = builtin("oreganized:lead_shrapnel", Registries.PARTICLE_TYPE);

    public static final OptRegSupplier<MobEffect> STUNNED_EFFECT = builtin("oreganized:stunned", Registries.MOB_EFFECT);

    public static final OptHolderRef<Enchantment> END_VEIL = datapack("betterend:end_veil", Registries.ENCHANTMENT);


    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T> OptRegSupplier<@Nullable T> builtin(String name, ResourceKey<Registry<T>> registry) {
        return  OptRegSupplier.of(ResourceLocation.tryParse(name), registry);
    }

    private static <T> OptHolderRef<@Nullable T> datapack(String name, ResourceKey<Registry<T>> registry) {
        return  OptHolderRef.of(ResourceLocation.tryParse(name), registry);
    }

}
