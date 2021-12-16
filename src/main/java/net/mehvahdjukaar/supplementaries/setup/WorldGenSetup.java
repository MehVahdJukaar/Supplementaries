package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.world.generation.FeaturesRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

public class WorldGenSetup {

    public static void registerStuffToBiomes(BiomeLoadingEvent event) {
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
        if(category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE) {

            if(ServerConfigs.spawn.URN_PILE_ENABLED.get()) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, FeaturesRegistry.PLACED_CAVE_URNS);
            }

            if (ServerConfigs.spawn.WILD_FLAX_ENABLED.get()) {

                ResourceLocation res = event.getName();
                if (res != null) {

                    ResourceKey<Biome> key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, res);
                    Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
                    if (types.contains(SANDY) && (types.contains(HOT) || types.contains(DRY)) || types.contains(RIVER)) {
                        event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FeaturesRegistry.PLACED_WILD_FLAX_PATCH);
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