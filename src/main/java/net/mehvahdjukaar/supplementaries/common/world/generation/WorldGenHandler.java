package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

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

    public static void onRegisterAdditional(){
        CaveFilter.init();
    }

    public static void addStuffToBiomes(BiomeLoadingEvent event) {

        Biome.BiomeCategory category = event.getCategory();
        if (category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE) {

            if (ServerConfigs.spawn.URN_PILE_ENABLED.get()) {
                if (!ServerConfigs.spawn.URN_BIOME_BLACKLIST.get().contains(event.getName().toString())) {
                    if(!event.getName().getNamespace().equals("twilightforest")) //TODO: find a better way to handle dimensons with weird land height
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
