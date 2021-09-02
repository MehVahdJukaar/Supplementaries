package net.mehvahdjukaar.supplementaries.world.structures;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;

public class ConfiguredFeatures {

    public static final BlockClusterFeatureConfig WILD_FLAX_CONFIG = (new BlockClusterFeatureConfig.Builder(
            new SimpleBlockStateProvider(ModRegistry.FLAX_WILD.get().defaultBlockState()),
            new SimpleBlockPlacer()))
            .tries(35).xspread(4).yspread(0).zspread(4).noProjection()
            .needWater().whitelist(ImmutableSet.of(Blocks.SAND,Blocks.RED_SAND)).build();

    /**
     * Static instance of our structure so we can reference it and add it to biomes easily.
     */
    public static final StructureFeature<?, ?> CONFIGURED_WAY_SIGN = StructureRegistry.WAY_SIGN.get().configured(IFeatureConfig.NONE);

    public static final ConfiguredFeature<?, ?> CONFIGURED_WILD_FLAX = Feature.RANDOM_PATCH.configured(WILD_FLAX_CONFIG)
            .decorated(Features.Placements.HEIGHTMAP_DOUBLE_SQUARE).count(20);

    /**
     * Registers the configured structure which is what gets added to the biomes.
     * Noticed we are not using a forge registry because there is none for configured structures.
     *
     * We can register configured structures at any time before a world is clicked on and made.
     * But the best time to register configured features by code is honestly to do it in FMLCommonSetupEvent.
     */
    public static void register() {


        Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,
                new ResourceLocation(Supplementaries.MOD_ID, "configured_way_sign"), CONFIGURED_WAY_SIGN);

        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(Supplementaries.MOD_ID, "configured_wild_flax"), CONFIGURED_WILD_FLAX);


        /* Ok so, this part may be hard to grasp but basically, just add your structure to this to
         * prevent any sort of crash or issue with other mod's custom ChunkGenerators. If they use
         * FlatGenerationSettings.STRUCTURES in it and you don't add your structure to it, the game
         * could crash later when you attempt to add the StructureSeparationSettings to the dimension.
         *
         * (It would also crash with superflat worldtype if you omit the below line
         * and attempt to add the structure's StructureSeparationSettings to the world)
         *
         * Note: If you want your structure to spawn in superflat, remove the FlatChunkGenerator check
         * in StructureTutorialMain.addDimensionalSpacing and then create a superflat world, exit it,
         * and re-enter it and your structures will be spawning. I could not figure out why it needs
         * the restart but honestly, superflat is really buggy and shouldn't be your main focus in my opinion.
         */
        FlatGenerationSettings.STRUCTURE_FEATURES.put(StructureRegistry.WAY_SIGN.get(), CONFIGURED_WAY_SIGN);
    }
}
