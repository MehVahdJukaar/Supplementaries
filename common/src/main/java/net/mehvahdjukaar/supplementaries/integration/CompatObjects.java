package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.items.EndermanHeadItem;
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

    public static final Supplier<Block> COPPER_LANTERN = makeCompatObject("suppsquared:copper_lantern", BuiltInRegistries.BLOCK);
    public static final Supplier<Block> CHANDELIER = makeCompatObject("decorative_blocks:chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SOUL_CHANDELIER = makeCompatObject("decorative_blocks:soul_chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> GLOW_CHANDELIER = makeCompatObject("muchmoremodcompat:glow_chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> ENDER_CHANDELIER = makeCompatObject("decorative_blocks_abnormals:ender_chandelier", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SAPPY_MAPLE_LOG = makeCompatObject("autumnity:sappy_maple_log", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SAPPY_MAPLE_WOOD = makeCompatObject("autumnity:sappy_maple_wood", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> TATER = makeCompatObject("quark:tiny_potato", BuiltInRegistries.BLOCK);


    public static final Supplier<ParticleType<?>> ENDER_FLAME = makeCompatObject("endergetic:ender_flame", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<ParticleType<?>> GLOW_FLAME = makeCompatObject("infernalexp:glowstone_sparkle", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<ParticleType<?>> NETHER_BRASS_FLAME = makeCompatObject("architects_palette:green_flame", BuiltInRegistries.PARTICLE_TYPE);


    public static final Supplier<ParticleType<?>> SMALL_SOUL_FLAME = makeCompatObject("buzzier_bees:small_soul_fire_flame", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<Item> SOUL_CANDLE_ITEM = makeCompatObject("buzzier_bees:soul_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SOUL_CANDLE = makeCompatObject("buzzier_bees:soul_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<Item> SPECTACLE_CANDLE_ITEM = makeCompatObject("cave_enhancements:spectacle_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SPECTACLE_CANDLE = makeCompatObject("cave_enhancements:spectacle_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> SUGAR_WATER = makeCompatObject("the_bumblezone:sugar_water_block", BuiltInRegistries.BLOCK);

    public static final Supplier<Item> TOME = makeCompatObject("quark:ancient_tome", BuiltInRegistries.ITEM);

    public static final Supplier<Block> RICH_SOIL = makeCompatObject("farmersdelight:rich_soil", BuiltInRegistries.BLOCK);
    public static final Supplier<Block> TOMATOES = makeCompatObject("farmersdelight:tomatoes", BuiltInRegistries.BLOCK);

    public static final Supplier<Block> RICH_SOUL_SOIL = makeCompatObject("nethers_delight:rich_soul_soil", BuiltInRegistries.BLOCK);

    public static final Supplier<ParticleType<?>> SHARPNEL = makeCompatObject("oreganized:lead_shrapnel", BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<MobEffect> STUNNED_EFFECT = makeCompatObject("oreganized:stunned", BuiltInRegistries.MOB_EFFECT);

    public static final Supplier<Enchantment> END_VEIL = makeCompatObject("betterend:end_veil", BuiltInRegistries.ENCHANTMENT);



    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T> Supplier<@Nullable T> makeCompatObject(String name, Registry<T> registry) {
        return Suppliers.memoize(() -> registry.getOptional(new ResourceLocation(name)).orElse(null));
    }

}
