package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CompatObjects {

    //these can return null on get instead of throwing

    public static final Supplier<Block> WALL_LANTERN = make("amendments:wall_lantern", BuiltInRegistries.BLOCK);
    public static final Supplier<Block> DIRECTIONAL_CAKE = make("amendments:directional_cake", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> COPPER_LANTERN = make("suppsquared:copper_lantern", BuiltInRegistries.BLOCK);
    public static final Supplier<Block> CHANDELIER = make("decorative_blocks:chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SOUL_CHANDELIER = make("decorative_blocks:soul_chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> GLOW_CHANDELIER = make("muchmoremodcompat:glow_chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> ENDER_CHANDELIER = make("decorative_blocks_abnormals:ender_chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SAPPY_MAPLE_LOG = make("autumnity:sappy_maple_log", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SAPPY_MAPLE_WOOD = make("autumnity:sappy_maple_wood", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> TATER = make("quark:tiny_potato", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> TOMATO_CROP = make("farmersdelight:tomatoes", BuiltInRegistries.BLOCK);


    public static final Supplier<ParticleType<?>> ENDER_FLAME = make("endergetic:ender_flame", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<ParticleType<?>> GLOW_FLAME = make("infernalexp:glowstone_sparkle", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<ParticleType<?>> NETHER_BRASS_FLAME = make("architects_palette:green_flame", BuiltInRegistries.PARTICLE_TYPE);


    public static final Supplier<ParticleType<?>> SMALL_SOUL_FLAME = make("buzzier_bees:small_soul_fire_flame", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<Item> SOUL_CANDLE_ITEM = make("buzzier_bees:soul_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SOUL_CANDLE = make("buzzier_bees:soul_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<Item> SPECTACLE_CANDLE_ITEM = make("cave_enhancements:spectacle_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SPECTACLE_CANDLE = make("cave_enhancements:spectacle_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SUGAR_WATER = make("the_bumblezone:sugar_water_block", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> MILK_CAULDRON = make("rats:milk_cauldron", BuiltInRegistries.BLOCK);

    public static final Supplier<Item> TOME = make("quark:ancient_tome", BuiltInRegistries.ITEM);
    public static final Supplier<Item> GENE_BOOK = make("horse_colors:gene_book", BuiltInRegistries.ITEM);

    public static final Supplier<Item> BARBARIC_HELMET = make("goated:barbaric_helmet", BuiltInRegistries.ITEM);

    public static final Supplier<Item> ATLAS = make("map_atlases:atlas", BuiltInRegistries.ITEM);

    public static final Supplier<Block> RICH_SOIL = make("farmersdelight:rich_soil", BuiltInRegistries.BLOCK);
    public static final Supplier<Block> TOMATOES = make("farmersdelight:tomatoes", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> RICH_SOUL_SOIL = make("nethers_delight:rich_soul_soil", BuiltInRegistries.BLOCK);

    public static final Supplier<ParticleType<?>> SHARPNEL = make("oreganized:lead_shrapnel", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<MobEffect> STUNNED_EFFECT = make("oreganized:stunned", BuiltInRegistries.MOB_EFFECT);

    public static final Supplier<Enchantment> END_VEIL = make("betterend:end_veil", BuiltInRegistries.ENCHANTMENT);


    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T> Supplier<@Nullable T> make(String name, Registry<T> registry) {
        return Suppliers.memoize(() -> registry.getOptional(new ResourceLocation(name)).orElse(null));
    }

}
