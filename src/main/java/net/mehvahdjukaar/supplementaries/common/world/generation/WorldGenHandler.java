package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.world.generation.structure.WaySignStructure;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

public class WorldGenHandler {


    public static void registerBus(IEventBus modEventBus) {
        // For registration and registerBus stuff.
        ModStructures.STRUCTURES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventPriority.NORMAL, WorldGenHandler::addStuffToBiomes);
    }

    public static void onInit(){
        ModStructureSets.init();
        ModPlacedFeatures.init();
    }


/*
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
                        associateBiomeToConfiguredStructure(STStructureToMultiMap, ModConfiguredFeatures.CONFIGURED_WAY_SIGN_STRUCTURE, biomeEntry.getKey());
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
            tempMap.putIfAbsent(ModStructures.WAY_SIGN.get(), StructureSettings.DEFAULTS.get(ModStructures.WAY_SIGN.get()));
            worldStructureSettings.structureConfig = tempMap;
        }
    }
    */


    public static void addStuffToBiomes(BiomeLoadingEvent event) {

        Biome.BiomeCategory category = event.getCategory();
        if (category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE) {

            if (ServerConfigs.spawn.URN_PILE_ENABLED.get()) {
                if (!ServerConfigs.spawn.URN_BIOME_BLACKLIST.get().contains(event.getName().toString())) {
                    event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.PLACED_CAVE_URNS);
                }
            }

            if (ServerConfigs.spawn.WILD_FLAX_ENABLED.get()) {

                ResourceLocation res = event.getName();
                if (res != null && category != Biome.BiomeCategory.UNDERGROUND) {

                    ResourceKey<Biome> key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, res);
                    Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
                    if (types.contains(SANDY) && (types.contains(HOT) || types.contains(DRY)) || types.contains(RIVER)) {
                        event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PLACED_WILD_FLAX_PATCH);
                    }
                }
            }
        }
    }

}
