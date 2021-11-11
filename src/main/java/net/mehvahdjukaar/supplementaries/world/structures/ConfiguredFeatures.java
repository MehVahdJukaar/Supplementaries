package net.mehvahdjukaar.supplementaries.world.structures;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Features;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.blockplacers.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class ConfiguredFeatures {

    public static final RandomPatchConfiguration WILD_FLAX_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(
            new SimpleStateProvider(ModRegistry.FLAX_WILD.get().defaultBlockState()),
            new SimpleBlockPlacer()))
            .tries(35).xspread(4).yspread(0).zspread(4).noProjection()
            .needWater().whitelist(ImmutableSet.of(Blocks.SAND, Blocks.RED_SAND)).build();

    /**
     * Static instance of our structure, so we can reference it and add it to biomes easily.
     */
    public static final ConfiguredStructureFeature<?, ?> CONFIGURED_WAY_SIGN = StructureRegistry.WAY_SIGN.get().configured(FeatureConfiguration.NONE);

    public static final ConfiguredFeature<?, ?> CONFIGURED_WILD_FLAX = Feature.RANDOM_PATCH.configured(WILD_FLAX_CONFIG)
            .decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(25);

    /**
     * Registers the configured structure which is what gets added to the biomes.
     * Noticed we are not using a forge registry because there is none for configured structures.
     * <p>
     * We can register configured structures at any time before a world is clicked on and made.
     * But the best time to register configured features by code is honestly to do it in FMLCommonSetupEvent.
     */
    public static void register() {

        Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE,
                new ResourceLocation(Supplementaries.MOD_ID, "configured_way_sign"), CONFIGURED_WAY_SIGN);

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(Supplementaries.MOD_ID, "configured_wild_flax"), CONFIGURED_WILD_FLAX);
    }
}
