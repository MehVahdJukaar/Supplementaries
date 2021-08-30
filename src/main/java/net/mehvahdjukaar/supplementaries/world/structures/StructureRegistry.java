package net.mehvahdjukaar.supplementaries.world.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.world.structures.processors.SignDataProcessor;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureRegistry {


    public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Supplementaries.MOD_ID);

    //do NOT change this
    public static final RegistryObject<Structure<NoFeatureConfig>> WAY_SIGN = STRUCTURES.register("way_sign",
            () -> (new WaySignStructure(NoFeatureConfig.CODEC)));


    //mod init. registers events
    public static void init(IEventBus bus) {
        // For registration and init stuff.
        STRUCTURES.register(bus);

        FeaturesHandler.FEATURES.register(bus);

        // For events that happen after initialization. This is probably going to be used a lot.
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(EventPriority.NORMAL, StructureRegistry::addDimensionalSpacing);

        // The comments for BiomeLoadingEvent and StructureSpawnListGatherEvent says to do HIGH for additions.
        forgeBus.addListener(EventPriority.HIGH, StructureRegistry::addStructureToBiome);
    }

    //common seutp
    public static void setup() {
        setupStructures();
        ConfiguredFeatures.register();

        SignDataProcessor.register();
    }


    public static void addStructureToBiome(final BiomeLoadingEvent event) {
        /*
         * Add our structure to all biomes including other modded biomes.
         * You can skip or add only to certain biomes based on stuff like biome category,
         * temperature, scale, precipitation, mod id, etc. All kinds of options!
         *
         * You can even use the BiomeDictionary as well! To use BiomeDictionary, do
         * RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName()) to get the biome's
         * registrykey. Then that can be fed into the dictionary to get the biome's types.
         */
        if (BiomeDictionary.hasType(RegistryKey.create(Registry.BIOME_REGISTRY, event.getName()), BiomeDictionary.Type.OCEAN)
                || ServerConfigs.spawn.ROAD_SIGN_DISTANCE_MIN.get() == 1001) return;

        event.getGeneration().getStructures().add(() -> ConfiguredFeatures.CONFIGURED_WAY_SIGN);
    }

    /**
     * Will go into the world's chunkgenerator and manually add our structure spacing.
     * If the spacing is not added, the structure doesn't spawn.
     * <p>
     * Use this for dimension blacklists for your structure.
     * (Don't forget to attempt to remove your structure too from the map if you are blacklisting that dimension!)
     * (It might have your structure in it already.)
     * <p>
     * Basically use this to make absolutely sure the chunkgenerator can or cannot spawn your structure.
     */
    private static Method GETCODEC_METHOD;

    public static void addDimensionalSpacing(final WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();

            /*
             * Skip Terraforged's chunk generator as they are a special case of a mod locking down their chunkgenerator.
             * They will handle your structure spacing for your if you add to WorldGenRegistries.NOISE_SETTINGS in FMLCommonSetupEvent.
             * This here is done with reflection as this tutorial is not about setting up and using Mixins.
             * If you are using mixins, you can call getCodec with an invoker mixin instead of using reflection.
             */
            try {
                if (GETCODEC_METHOD == null)
                    GETCODEC_METHOD = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey((Codec<? extends ChunkGenerator>) GETCODEC_METHOD.invoke(serverWorld.getChunkSource().generator));
                if (cgRL != null && cgRL.getNamespace().equals("terraforged")) return;
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Was unable to check if " + serverWorld.dimension().getRegistryName() + " is using Terraforged's ChunkGenerator.");
            }

            /*
             * Prevent spawning our structure in Vanilla's superflat world as
             * people seem to want their superflat worlds free of modded structures.
             * Also that vanilla superflat is really tricky and buggy to work with in my experience.
             */
            if (serverWorld.getChunkSource().generator instanceof FlatChunkGenerator &&
                    serverWorld.dimension().equals(World.OVERWORLD)) {
                return;
            }
            //serverWorld.getChunkSource().generator.getBiomeSource().possibleBiomes().stream().forEach(b->b.cange);


            //adding only to biomes and dimensions that can generate vanilla villages


            boolean isVillageDimension = false;

            //TODO: might be a bug here with .canGenerateStructure
            try{
                BiomeProvider provider = serverWorld.getChunkSource().generator.getBiomeSource();
                List<Biome> biomes = provider.possibleBiomes();
                if(biomes.contains(null)){
                    Supplementaries.LOGGER.throwing(new Exception("something went wrong: found a null biome in the biome provider"));
                }

                isVillageDimension = provider.canGenerateStructure(Structure.VILLAGE);
            }catch (Exception ignored){
                Supplementaries.LOGGER.throwing(new Exception("failed to add structure to biomes: something went wrong, might be some other mod bug"));
            }

            if (isVillageDimension && serverWorld.dimensionType().natural() && ServerConfigs.spawn.ROAD_SIGN_DISTANCE_MIN.get() != 1001) {

                /*
                 * putIfAbsent so people can override the spacing with dimension datapacks themselves if they wish to customize spacing more precisely per dimension.
                 *
                 * NOTE: if you add per-dimension spacing configs, you can't use putIfAbsent as WorldGenRegistries.NOISE_SETTINGS in FMLCommonSetupEvent
                 * already added your default structure spacing to some dimensions. You would need to override the spacing with .put(...)
                 * And if you want to do dimension blacklisting, you need to remove the spacing entry entirely from the map below to prevent generation safely.
                 */
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld.getChunkSource().generator.getSettings().structureConfig());
                tempMap.putIfAbsent(WAY_SIGN.get(), DimensionStructuresSettings.DEFAULTS.get(WAY_SIGN.get()));
                serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
            } else {
                //removing it from the map if it's there already for some damn reason
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld.getChunkSource().generator.getSettings().structureConfig());
                tempMap.remove(WAY_SIGN.get());
                serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
            }
        }
    }


    /**
     * This is where we set the rarity of your structures and determine if land conforms to it.
     * See the comments in below for more details.
     */
    private static void setupStructures() {
        setupMapSpacingAndLand(
                WAY_SIGN.get(), /* The instance of the structure */
                new StructureSeparationSettings(ServerConfigs.spawn.ROAD_SIGN_DISTANCE_AVR.get() /* average distance apart in chunks between spawn attempts */,
                        ServerConfigs.spawn.ROAD_SIGN_DISTANCE_MIN.get() /* minimum distance apart in chunks between spawn attempts */,
                        431041527 /* this modifies the seed of the structure so no two structures always spawn over each-other. Make this large and unique. */),
                true);


        // Add more structures here and so on
    }

    /**
     * Adds the provided structure to the registry, and adds the separation settings.
     * The rarity of the structure is determined based on the values passed into
     * this method in the structureSeparationSettings argument. Called by registerFeatures.
     */
    private static <F extends Structure<?>> void setupMapSpacingAndLand(
            F structure,
            StructureSeparationSettings structureSeparationSettings,
            boolean transformSurroundingLand) {
        /*
         * We need to add our structures into the map in Structure alongside vanilla
         * structures or else it will cause errors. Called by registerStructure.
         *
         * If the registration is setup properly for the structure,
         * getRegistryName() should never return null.
         */
        try {
            Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);
            Structure.STRUCTURES_REGISTRY.get(structure.getRegistryName().toString()).getRegistryName();
        } catch (Exception e) {
            Supplementaries.LOGGER.throwing(new Exception("failed to register way sign structure: " + e + ". this is a bug"));
        }

        /*
         * Whether surrounding land will be modified automatically to conform to the bottom of the structure.
         * Basically, it adds land at the base of the structure like it does for Villages and Outposts.
         * Doesn't work well on structure that have pieces stacked vertically or change in heights.
         *
         * Note: The air space this method will create will be filled with water if the structure is below sealevel.
         * This means this is best for structure above sealevel so keep that in mind.
         */
        if (transformSurroundingLand) {
            Structure.NOISE_AFFECTING_FEATURES =
                    ImmutableList.<Structure<?>>builder()
                            .addAll(Structure.NOISE_AFFECTING_FEATURES)
                            .add(structure)
                            .build();
        }

        /*
         * Adds the structure's spacing into a default structure spacing map that other mods can utilize.
         *
         * However, while it does propagate the spacing to some correct dimensions form this map,
         * it seems it doesn't always work for code made dimensions as they read from this list beforehand.
         *
         * Instead, we will use the WorldEvent.Load event in StructureTutorialMain to add the structure
         * spacing from this list into that dimension or do dimension blacklisting properly. We also use
         * our entry in DimensionStructuresSettings.field_236191_b_ in WorldEvent.Load as well.
         */
        DimensionStructuresSettings.DEFAULTS =
                ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                        .putAll(DimensionStructuresSettings.DEFAULTS)
                        .put(structure, structureSeparationSettings)
                        .build();


        /*
         * There are very few mods that relies on seeing your structure in the noise settings registry before the world is made.
         *
         * This is best done here in FMLCommonSetupEvent after you created your configuredstructures.
         * You may see some mods add their spacings to DimensionSettings.field_242740_q instead of the NOISE_SETTINGS loop below but
         * that field only applies for the default overworld and won't add to other worldtypes or dimensions (like amplified or Nether).
         * So yeah, don't do DimensionSettings.field_242740_q. Use the NOISE_SETTINGS loop below instead.
         */
        WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
            Map<Structure<?>, StructureSeparationSettings> structureMap = settings.getValue().structureSettings().structureConfig;

            /*
             * Pre-caution in case a mod makes the structure map immutable like datapacks do.
             * I take no chances myself. You never know what another mods does...
             */
            if (structureMap instanceof ImmutableMap) {
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, structureSeparationSettings);
                settings.getValue().structureSettings().structureConfig = tempMap;
            } else {
                structureMap.put(structure, structureSeparationSettings);
            }
        });
    }

}
