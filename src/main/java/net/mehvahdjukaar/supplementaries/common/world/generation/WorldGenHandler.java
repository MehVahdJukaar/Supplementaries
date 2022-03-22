package net.mehvahdjukaar.supplementaries.common.world.generation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.mixins.accessors.ChunkGeneratorAccessor;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

public class WorldGenHandler {


    public static void registerBus(IEventBus modEventBus) {
        // For registration and registerBus stuff.
        StructuresRegistry.STRUCTURES.register(modEventBus);
        FeaturesRegistry.FEATURES.register(modEventBus);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventPriority.NORMAL, WorldGenHandler::addDimensionalSpacing);
        bus.addListener(EventPriority.NORMAL, WorldGenHandler::addStuffToBiomes);
    }

    /**
     * Here, setupStructures will be ran after registration of all structures are finished.
     * This is important to be done here so that the Deferred Registry has already ran and
     * registered/created our structure for us.
     * <p>
     * Once after that structure instance is made, we then can now do the rest of the setup
     * that requires a structure instance such as setting the structure spacing, creating the
     * configured structure instance, and more.
     */
    public static void setup(final FMLCommonSetupEvent event) {
        StructuresRegistry.setupStructures();
        ConfiguredFeaturesRegistry.registerFeatures();
    }

    /**
     * Tells the chunkgenerator which biomes our structure can spawn in.
     * Will go into the world's chunkgenerator and manually add our structure spacing.
     * If the spacing is not added, the structure doesn't spawn.
     * <p>
     * Use this for dimension blacklists for your structure.
     * (Don't forget to attempt to remove your structure too from the map if you are blacklisting that dimension!)
     * (It might have your structure in it already.)
     * <p>
     * Basically use this to make absolutely sure the chunkgenerator can or cannot spawn your structure.
     */
    public static void addDimensionalSpacing(final WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            //skips superflat and terraforged and non overworld
            if (chunkGenerator instanceof ChunkGeneratorAccessor accessor) {
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey(accessor.invokeCodec());
                if (cgRL != null && cgRL.getNamespace().equals("terraforged")) return;
            }
            if (chunkGenerator instanceof FlatLevelSource || !serverLevel.dimension().equals(Level.OVERWORLD)) {
                return;
            }
            if (ServerConfigs.spawn.ROAD_SIGN_DISTANCE_MIN.get() == 1001) return;

            StructureSettings worldStructureSettings = chunkGenerator.getSettings();


            // Create a mutable map we will use for easier adding to biomes
            HashMap<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> STStructureToMultiMap = new HashMap<>();

            // Add the resourcekey of all biomes that this Configured Structure can spawn in.
            for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : serverLevel.registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY).entrySet()) {
                // Skip all ocean, end, nether, and none category biomes.
                // You can do checks for other traits that the biome has.

                //village check
                StructureFeatureConfiguration structurefeatureconfiguration = worldStructureSettings.getConfig(StructureFeature.VILLAGE);
                ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> immutablemultimap = worldStructureSettings.structures(StructureFeature.VILLAGE);
                if (structurefeatureconfiguration != null && !immutablemultimap.isEmpty()) {

                    Biome.BiomeCategory biomeCategory = biomeEntry.getValue().getBiomeCategory();
                    if (biomeCategory != Biome.BiomeCategory.OCEAN && biomeCategory != Biome.BiomeCategory.THEEND &&
                            biomeCategory != Biome.BiomeCategory.RIVER &&
                            biomeCategory != Biome.BiomeCategory.UNDERGROUND &&
                            biomeCategory != Biome.BiomeCategory.NETHER && biomeCategory != Biome.BiomeCategory.NONE) {
                        associateBiomeToConfiguredStructure(STStructureToMultiMap, ConfiguredFeaturesRegistry.CONFIGURED_WAY_SIGN_STRUCTURE, biomeEntry.getKey());
                    }
                }

            }

            // Grab the map that holds what ConfigureStructures a structure has and what biomes it can spawn in and merge with ours.
            ImmutableMap.Builder<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> tempStructureToMultiMap = ImmutableMap.builder();
            worldStructureSettings.configuredStructures.entrySet().stream().filter(entry -> !STStructureToMultiMap.containsKey(entry.getKey())).forEach(tempStructureToMultiMap::put);
            STStructureToMultiMap.forEach((key, value) -> tempStructureToMultiMap.put(key, ImmutableMultimap.copyOf(value)));

            worldStructureSettings.configuredStructures = tempStructureToMultiMap.build();


            //dont forget this part
            Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(worldStructureSettings.structureConfig());
            tempMap.putIfAbsent(StructuresRegistry.WAY_SIGN.get(), StructureSettings.DEFAULTS.get(StructuresRegistry.WAY_SIGN.get()));
            worldStructureSettings.structureConfig = tempMap;
        }
    }

    /**
     * Helper method that handles setting up the map to multimap relationship to help prevent issues.
     */
    private static void associateBiomeToConfiguredStructure(Map<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> STStructureToMultiMap,
                                                            ConfiguredStructureFeature<?, ?> configuredStructureFeature,
                                                            ResourceKey<Biome> biomeRegistryKey) {
        STStructureToMultiMap.putIfAbsent(configuredStructureFeature.feature, HashMultimap.create());
        HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> configuredStructureToBiomeMultiMap = STStructureToMultiMap.get(configuredStructureFeature.feature);
        if (configuredStructureToBiomeMultiMap.containsValue(biomeRegistryKey)) {
            Supplementaries.LOGGER.error("""
                                Detected 2 ConfiguredStructureFeatures that share the same base StructureFeature trying to be added to same biome. One will be prevented from spawning.
                                This issue happens with vanilla too and is why a Snowy Village and Plains Village cannot spawn in the same biome because they both use the Village base structure.
                                The two conflicting ConfiguredStructures are: {}, {}
                                The biome that is attempting to be shared: {}
                            """,
                    BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructureFeature),
                    BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructureToBiomeMultiMap.entries().stream().filter(e -> e.getValue() == biomeRegistryKey).findFirst().get().getKey()),
                    biomeRegistryKey
            );
        } else {
            configuredStructureToBiomeMultiMap.put(configuredStructureFeature, biomeRegistryKey);
        }
    }

    public static void addStuffToBiomes(BiomeLoadingEvent event) {
//        if(!RegistryConfigs.reg.FIREFLY_ENABLED.get())return;
//        if (event.getName() != null) {
//            Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
//            if (biome != null) {
//                //RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, event.getName());
//                ResourceLocation biomeRegistryName = biome.getRegistryName();//ForgeRegistries.BIOMES.getKey(biome);
//
//                if (biomeRegistryName != null) {
//                    String biomeNamespace = biomeRegistryName.getNamespace();
//                    if (ServerConfigs.spawn.FIREFLY_MOD_WHITELIST.get().contains(biomeNamespace) ||
//                            ServerConfigs.spawn.FIREFLY_BIOMES.get().contains(biomeRegistryName.toString())) {
//                        int min = ServerConfigs.spawn.FIREFLY_MIN.get();
//                        int max = Math.max(min,ServerConfigs.spawn.FIREFLY_MAX.get());
//
//                        event.getSpawns().getSpawner(MobCategory.AMBIENT).add(new MobSpawnSettings.SpawnerData(
//                                ModRegistry.FIREFLY_TYPE.get(), ServerConfigs.spawn.FIREFLY_WEIGHT.get(),
//                                min,max));
//                    }
//
//                }
//            }
//        }

        Biome.BiomeCategory category = event.getCategory();
        if (category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE) {

            if (ServerConfigs.spawn.URN_PILE_ENABLED.get()) {
                if (!ServerConfigs.spawn.URN_BIOME_BLACKLIST.get().contains(event.getName().toString())) {
                    event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, ConfiguredFeaturesRegistry.PLACED_CAVE_URNS);
                }
            }

            if (ServerConfigs.spawn.WILD_FLAX_ENABLED.get()) {

                ResourceLocation res = event.getName();
                if (res != null && category != Biome.BiomeCategory.UNDERGROUND) {

                    ResourceKey<Biome> key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, res);
                    Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
                    if (types.contains(SANDY) && (types.contains(HOT) || types.contains(DRY)) || types.contains(RIVER)) {
                        event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ConfiguredFeaturesRegistry.PLACED_WILD_FLAX_PATCH);
                    }
                }
            }
        }
    }

    public static void registerMobSpawns() {

//        if(RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
//            SpawnPlacements.register(ModRegistry.FIREFLY_TYPE.get(), SpawnPlacements.Type.NO_RESTRICTIONS,
//                    Heightmap.Types.MOTION_BLOCKING, FireflyEntity::canSpawnOn);
//        }
    }
}
