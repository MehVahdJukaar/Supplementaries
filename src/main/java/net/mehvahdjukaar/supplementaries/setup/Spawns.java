package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
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
                ResourceLocation biomeres = biome.getRegistryName();//ForgeRegistries.BIOMES.getKey(biome);

                if (biomeres != null) {
                    String modbiomes = biomeres.getNamespace();
                    if (ServerConfigs.spawn.FIREFLY_MOD_WHITELIST.get().contains(modbiomes) ||
                            ServerConfigs.spawn.FIREFLY_BIOMES.get().contains(biomeres.toString())) {
                        event.getSpawns().getSpawner(EntityClassification.AMBIENT).add(new MobSpawnInfo.Spawners(
                                Registry.FIREFLY_TYPE.get(), ServerConfigs.spawn.FIREFLY_WEIGHT.get(),
                                ServerConfigs.spawn.FIREFLY_MIN.get(),
                                ServerConfigs.spawn.FIREFLY_MAX.get()));
                    }

                }
            }
        }
    }

    public static void registerSpawningStuff(){

        if(RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
            EntitySpawnPlacementRegistry.register(Registry.FIREFLY_TYPE.get(), EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS,
                    Heightmap.Type.MOTION_BLOCKING, FireflyEntity::canSpawnOn);
        }
    }





}