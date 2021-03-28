package net.mehvahdjukaar.supplementaries.world.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
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
import java.util.Map;
import java.util.function.Supplier;

public class StructureRegistry {

    /**
     * We are using the Deferred Registry system to register our structure as this is the preferred way on Forge.
     * This will handle registering the base structure for us at the correct time so we don't have to handle it ourselves.
     *
     * HOWEVER, do note that Deferred Registries only work for anything that is a Forge Registry. This means that
     * configured structures and configured features need to be registered directly to WorldGenRegistries as there
     * is no Deferred Registry system for them.
     */
    public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Supplementaries.MOD_ID);

    /**
     * Registers the structure itself and sets what its path is. In this case, the
     * structure will have the resourcelocation of structure_tutorial:run_down_house.
     *
     * It is always a good idea to register your Structures so that other mods and datapacks can
     * use them too directly from the registries. It great for mod/datapacks compatibility.
     *
     * IMPORTANT: Once you have set the name for your structure below and distributed your mod,
     * it should NEVER be changed or else it can cause worlds to become corrupted if they generated
     * any chunks with your mod with the old structure name. See MC-194811 in Mojang's bug tracker for details.
     *
     * Forge has an issue report here: https://github.com/MinecraftForge/MinecraftForge/issues/7363
     * Keep watch on that to know when it is safe to remove or change structure's registry names
     */
    public static final RegistryObject<Structure<NoFeatureConfig>> ROAD_SIGN = STRUCTURES.register("road_sign",
            () -> (new RoadSignStructure(NoFeatureConfig.CODEC)));

    /**
     * Static instance of our structure so we can reference it and add it to biomes easily.
     */
    public static Supplier<StructureFeature<?, ?>> CONFIGURED_ROAD_SIGN = ()->ROAD_SIGN.get().configured(IFeatureConfig.NONE);

    public static void init(IEventBus bus) {
        // For registration and init stuff.
        STRUCTURES.register(bus);

        // For events that happen after initialization. This is probably going to be used a lot.
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(EventPriority.NORMAL, StructureRegistry::addDimensionalSpacing);

        // The comments for BiomeLoadingEvent and StructureSpawnListGatherEvent says to do HIGH for additions.
        forgeBus.addListener(EventPriority.HIGH, StructureRegistry::addStructureToBiome);
    }

    /**
     * Here, setupStructures will be ran after registration of all structures are finished.
     * This is important to be done here so that the Deferred Registry has already ran and
     * registered/created our structure for us.
     *
     * Once after that structure instance is made, we then can now do the rest of the setup
     * that requires a structure instance such as setting the structure spacing, creating the
     * configured structure instance, and more.
     */
    public static void setup() {
        setupStructures();
        registerConfiguredStructures();
    }


    /**
     * This is the event you will use to add anything to any biome.
     * This includes spawns, changing the biome's looks, messing with its surfacebuilders,
     * adding carvers, spawning new features... etc
     *
     * Here, we will use this to add our structure to all biomes.
     */
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
        event.getGeneration().getStructures().add(() -> CONFIGURED_ROAD_SIGN.get());
    }

    /**
     * Will go into the world's chunkgenerator and manually add our structure spacing.
     * If the spacing is not added, the structure doesn't spawn.
     *
     * Use this for dimension blacklists for your structure.
     * (Don't forget to attempt to remove your structure too from the map if you are blacklisting that dimension!)
     * (It might have your structure in it already.)
     *
     * Basically use this to make absolutely sure the chunkgenerator can or cannot spawn your structure.
     */
    private static Method GETCODEC_METHOD;
    public static void addDimensionalSpacing(final WorldEvent.Load event) {
        if(event.getWorld() instanceof ServerWorld){
            ServerWorld serverWorld = (ServerWorld)event.getWorld();

            /*
             * Skip Terraforged's chunk generator as they are a special case of a mod locking down their chunkgenerator.
             * They will handle your structure spacing for your if you add to WorldGenRegistries.NOISE_SETTINGS in FMLCommonSetupEvent.
             * This here is done with reflection as this tutorial is not about setting up and using Mixins.
             * If you are using mixins, you can call getCodec with an invoker mixin instead of using reflection.
             */
            try {
                if(GETCODEC_METHOD == null) GETCODEC_METHOD = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey((Codec<? extends ChunkGenerator>) GETCODEC_METHOD.invoke(serverWorld.getChunkSource().generator));
                if(cgRL != null && cgRL.getNamespace().equals("terraforged")) return;
            }
            catch(Exception e){
                Supplementaries.LOGGER.error("Was unable to check if " + serverWorld.dimension().getRegistryName() + " is using Terraforged's ChunkGenerator.");
            }

            /*
             * Prevent spawning our structure in Vanilla's superflat world as
             * people seem to want their superflat worlds free of modded structures.
             * Also that vanilla superflat is really tricky and buggy to work with in my experience.
             */
            if(serverWorld.getChunkSource().generator instanceof FlatChunkGenerator &&
                    serverWorld.dimension().equals(World.OVERWORLD)){
                return;
            }

            //adding only to biomes and dimensions that can generate vanilla villages
            if(serverWorld.getChunkSource().generator.getBiomeSource().canGenerateStructure(Structure.VILLAGE)) {

                /*
                 * putIfAbsent so people can override the spacing with dimension datapacks themselves if they wish to customize spacing more precisely per dimension.
                 *
                 * NOTE: if you add per-dimension spacing configs, you can't use putIfAbsent as WorldGenRegistries.NOISE_SETTINGS in FMLCommonSetupEvent
                 * already added your default structure spacing to some dimensions. You would need to override the spacing with .put(...)
                 * And if you want to do dimension blacklisting, you need to remove the spacing entry entirely from the map below to prevent generation safely.
                 */
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld.getChunkSource().generator.getSettings().structureConfig());
                tempMap.putIfAbsent(ROAD_SIGN.get(), DimensionStructuresSettings.DEFAULTS.get(ROAD_SIGN.get()));
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
                ROAD_SIGN.get(), /* The instance of the structure */
                new StructureSeparationSettings(10 /* average distance apart in chunks between spawn attempts */,
                        5 /* minimum distance apart in chunks between spawn attempts */,
                        214986419 /* this modifies the seed of the structure so no two structures always spawn over each-other. Make this large and unique. */),
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
            boolean transformSurroundingLand)
    {
        /*
         * We need to add our structures into the map in Structure alongside vanilla
         * structures or else it will cause errors. Called by registerStructure.
         *
         * If the registration is setup properly for the structure,
         * getRegistryName() should never return null.
         */
        Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

        /*
         * Whether surrounding land will be modified automatically to conform to the bottom of the structure.
         * Basically, it adds land at the base of the structure like it does for Villages and Outposts.
         * Doesn't work well on structure that have pieces stacked vertically or change in heights.
         *
         * Note: The air space this method will create will be filled with water if the structure is below sealevel.
         * This means this is best for structure above sealevel so keep that in mind.
         */
        if(transformSurroundingLand){
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
            if(structureMap instanceof ImmutableMap){
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, structureSeparationSettings);
                settings.getValue().structureSettings().structureConfig = tempMap;
            }
            else{
                structureMap.put(structure, structureSeparationSettings);
            }
        });
    }







    /**
     * Registers the configured structure which is what gets added to the biomes.
     * Noticed we are not using a forge registry because there is none for configured structures.
     *
     * We can register configured structures at any time before a world is clicked on and made.
     * But the best time to register configured features by code is honestly to do it in FMLCommonSetupEvent.
     */
    private static void registerConfiguredStructures() {
        Registry<StructureFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE;
        Registry.register(registry, new ResourceLocation(Supplementaries.MOD_ID, "configured_road_sign"), CONFIGURED_ROAD_SIGN.get());

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
        FlatGenerationSettings.STRUCTURE_FEATURES.put(StructureRegistry.ROAD_SIGN.get(), CONFIGURED_ROAD_SIGN.get());
    }
}
