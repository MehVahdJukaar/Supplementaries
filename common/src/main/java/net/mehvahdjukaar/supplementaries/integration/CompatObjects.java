package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.misc.OptionalHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class CompatObjects {

    //these can return null on get instead of throwing

    public static final OptionalHolder<Block> WALL_LANTERN = make("amendments:wall_lantern", Registries.BLOCK);
    public static final OptionalHolder<Block> DIRECTIONAL_CAKE = make("amendments:directional_cake", Registries.BLOCK);

    public static final OptionalHolder<Block> COPPER_LANTERN = make("suppsquared:copper_lantern", Registries.BLOCK);
    public static final OptionalHolder<Block> CHANDELIER = make("decorative_blocks:chandelier", Registries.BLOCK);

    public static final OptionalHolder<Block> SOUL_CHANDELIER = make("decorative_blocks:soul_chandelier", Registries.BLOCK);

    public static final OptionalHolder<Block> GLOW_CHANDELIER = make("muchmoremodcompat:glow_chandelier", Registries.BLOCK);

    public static final OptionalHolder<Block> ENDER_CHANDELIER = make("decorative_blocks_abnormals:ender_chandelier", Registries.BLOCK);

    public static final OptionalHolder<Block> SAPPY_MAPLE_LOG = make("autumnity:sappy_maple_log", Registries.BLOCK);

    public static final OptionalHolder<Block> SAPPY_MAPLE_WOOD = make("autumnity:sappy_maple_wood", Registries.BLOCK);

    public static final OptionalHolder<Block> TATER = make("quark:tiny_potato", Registries.BLOCK);

    public static final OptionalHolder<Block> TOMATO_CROP = make("farmersdelight:tomatoes", Registries.BLOCK);


    public static final OptionalHolder<ParticleType<?>> ENDER_FLAME = make("endergetic:ender_flame", Registries.PARTICLE_TYPE);

    public static final OptionalHolder<ParticleType<?>> GLOW_FLAME = make("infernalexp:glowstone_sparkle", Registries.PARTICLE_TYPE);

    public static final OptionalHolder<ParticleType<?>> NETHER_BRASS_FLAME = make("architects_palette:green_flame", Registries.PARTICLE_TYPE);


    public static final OptionalHolder<ParticleType<?>> SMALL_SOUL_FLAME = make("buzzier_bees:small_soul_fire_flame", Registries.PARTICLE_TYPE);
    public static final OptionalHolder<ParticleType<?>> SMALL_CUPRIC_FLAME = make("caverns_and_chasms:small_cupric_fire_flame", Registries.PARTICLE_TYPE);
    public static final OptionalHolder<ParticleType<?>> SMALL_END_FLAME = make("endergetic:small_ender_fire_flame", Registries.PARTICLE_TYPE);

    public static final OptionalHolder<Item> SOUL_CANDLE_ITEM = make("buzzier_bees:soul_candle", Registries.ITEM);

    public static final OptionalHolder<Block> SOUL_CANDLE = make("buzzier_bees:soul_candle", Registries.BLOCK);

    public static final OptionalHolder<Item> SPECTACLE_CANDLE_ITEM = make("cave_enhancements:spectacle_candle", Registries.ITEM);

    public static final OptionalHolder<Block> SPECTACLE_CANDLE = make("cave_enhancements:spectacle_candle", Registries.BLOCK);

    public static final OptionalHolder<Block> SUGAR_WATER = make("the_bumblezone:sugar_water_block", Registries.BLOCK);

    public static final OptionalHolder<Block> MILK_CAULDRON = make("rats:milk_cauldron", Registries.BLOCK);

    public static final OptionalHolder<Item> TOME = make("quark:ancient_tome", Registries.ITEM);
    public static final OptionalHolder<Item> GENE_BOOK = make("horse_colors:gene_book", Registries.ITEM);

    public static final OptionalHolder<Item> BARBARIC_HELMET = make("goated:barbaric_helmet", Registries.ITEM);

    public static final OptionalHolder<Item> ATLAS = make("map_atlases:atlas", Registries.ITEM);

    public static final OptionalHolder<Block> RICH_SOIL = make("farmersdelight:rich_soil", Registries.BLOCK);
    public static final OptionalHolder<Block> TOMATOES = make("farmersdelight:tomatoes", Registries.BLOCK);

    public static final OptionalHolder<Block> RICH_SOUL_SOIL = make("nethers_delight:rich_soul_soil", Registries.BLOCK);

    public static final OptionalHolder<ParticleType<?>> SHARPNEL = make("oreganized:lead_shrapnel", Registries.PARTICLE_TYPE);

    public static final OptionalHolder<MobEffect> STUNNED_EFFECT = make("oreganized:stunned", Registries.MOB_EFFECT);

    public static final OptionalHolder<Enchantment> END_VEIL = make("betterend:end_veil", Registries.ENCHANTMENT);

    public static final OptionalHolder<EntityType<?>> ALEX_NUKE = make("alexcaves:nuclear_bomb", Registries.ENTITY_TYPE);

    public static final OptionalHolder<Block> NUKE_BLOCK = make("alexcaves:nuclear_bomb", Registries.BLOCK);


    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T> OptionalHolder<@Nullable T> make(String name, ResourceKey<Registry<T>> registry) {
        return OptionalHolder.of(ResourceLocation.tryParse(name), registry);
    }

}
