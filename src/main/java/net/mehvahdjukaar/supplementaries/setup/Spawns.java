package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID)
public class Spawns {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addSpawn(BiomeLoadingEvent event) {
        if(!RegistryConfigs.reg.FIREFLY_ENABLED.get())return;
        if (event.getName() != null) {
            Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
            if (biome != null) {
                //RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, event.getName());
                ResourceLocation biomeRegistryName = biome.getRegistryName();//ForgeRegistries.BIOMES.getKey(biome);

                if (biomeRegistryName != null) {
                    String biomeNamespace = biomeRegistryName.getNamespace();
                    if (ServerConfigs.spawn.FIREFLY_MOD_WHITELIST.get().contains(biomeNamespace) ||
                            ServerConfigs.spawn.FIREFLY_BIOMES.get().contains(biomeRegistryName.toString())) {
                        int min = ServerConfigs.spawn.FIREFLY_MIN.get();
                        int max = Math.max(min,ServerConfigs.spawn.FIREFLY_MAX.get());

                        event.getSpawns().getSpawner(MobCategory.AMBIENT).add(new MobSpawnSettings.SpawnerData(
                                ModRegistry.FIREFLY_TYPE.get(), ServerConfigs.spawn.FIREFLY_WEIGHT.get(),
                                min,max));
                    }

                }
            }
        }
    }

    public static void registerSpawningStuff(){

        if(RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
            SpawnPlacements.register(ModRegistry.FIREFLY_TYPE.get(), SpawnPlacements.Type.NO_RESTRICTIONS,
                    Heightmap.Types.MOTION_BLOCKING, FireflyEntity::canSpawnOn);
        }
    }





}