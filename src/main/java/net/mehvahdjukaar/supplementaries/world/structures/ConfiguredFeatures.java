package net.mehvahdjukaar.supplementaries.world.structures;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.UrnBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.blockplacers.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.CaveDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class ConfiguredFeatures {


    public static final RandomPatchConfiguration WILD_FLAX_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(
            new SimpleStateProvider(ModRegistry.FLAX_WILD.get().defaultBlockState()),
            new SimpleBlockPlacer()))
            .tries(35).xspread(4).yspread(0).zspread(4).noProjection()
            .needWater().whitelist(ImmutableSet.of(Blocks.SAND, Blocks.RED_SAND)).build();

    public static final RandomPatchConfiguration URN_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(
            new SimpleStateProvider(ModRegistry.URN.get().defaultBlockState().setValue(UrnBlock.TREASURE, true)),
            SimpleBlockPlacer.INSTANCE))
            .tries(4).xspread(4).yspread(1).zspread(4).noProjection()
            .build();
    /**
     * Static instance of our structure, so we can reference it and add it to biomes easily.
     */
    public static final ConfiguredStructureFeature<?, ?> CONFIGURED_WAY_SIGN = StructureRegistry.WAY_SIGN.get().configured(FeatureConfiguration.NONE);


    public static final RangeDecoratorConfiguration RANGE_BOTTOM_TO_40 = new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.bottom(), VerticalAnchor.absolute(40)));

    public static final ConfiguredFeature<?, ?> CONFIGURED_WILD_FLAX = Feature.RANDOM_PATCH.configured(WILD_FLAX_CONFIG)
            .decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(25);

    public static final ConfiguredFeature<?, ?> CONFIGURED_URN_PILE = Feature.RANDOM_PATCH.configured(URN_CONFIG)
            .decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.FLOOR, 12)))
            .range(RANGE_BOTTOM_TO_40).squared().count(8);

    /**
     * Registers the configured structure which is what gets added to the biomes.
     * Noticed we are not using a forge registry because there is none for configured structures.
     * <p>
     * We can register configured structures at any time before a world is clicked on and made.
     * But the best time to register configured features by code is honestly to do it in FMLCommonSetupEvent.
     */
    public static void register() {

        Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE,
                Supplementaries.res("configured_way_sign"), CONFIGURED_WAY_SIGN);

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
                Supplementaries.res("configured_wild_flax"), CONFIGURED_WILD_FLAX);

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
                Supplementaries.res("configured_urn"), CONFIGURED_URN_PILE);
    }
}
