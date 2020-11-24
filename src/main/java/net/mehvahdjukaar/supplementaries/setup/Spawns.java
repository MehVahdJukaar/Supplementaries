package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID)
public class Spawns {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addSpawn(BiomeLoadingEvent event) {
        if (event.getName() != null) {
            Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
            if (biome != null) {
                //RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, event.getName());
                ResourceLocation biomeres = ForgeRegistries.BIOMES.getKey(biome);

                if (ServerConfigs.cached.FIREFLY_BIOMES.contains(biomeres.getPath())){
                    //TODO:adjust this so they can spawn on more blocks but not underground
                    event.getSpawns().getSpawner(EntityClassification.AMBIENT).add(new MobSpawnInfo.Spawners(
                            Registry.FIREFLY_TYPE, ServerConfigs.cached.FIREFLY_WEIGHT,
                            ServerConfigs.cached.FIREFLY_MIN,
                            ServerConfigs.cached.FIREFLY_MAX));
                }

            }
        }
    }
}