package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
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
        if (event.getName() != null) {
            Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
            if (biome != null) {
                //RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, event.getName());
                ResourceLocation biomeres = ForgeRegistries.BIOMES.getKey(biome);

                if (ServerConfigs.cached.FIREFLY_BIOMES.contains(biomeres.toString())){
                    //TODO:adjust this so they can spawn on more blocks but not underground
                    event.getSpawns().getSpawner(EntityClassification.AMBIENT).add(new MobSpawnInfo.Spawners(
                            Registry.FIREFLY_TYPE, ServerConfigs.cached.FIREFLY_WEIGHT,
                            ServerConfigs.cached.FIREFLY_MIN,
                            ServerConfigs.cached.FIREFLY_MAX));
                }

            }
        }
    }

    public static void registerSpawningStuff(){
        //TODO:adjust this so they can spawn on more blocks but not underground
        EntitySpawnPlacementRegistry.register((EntityType<FireflyEntity>)Registry.FIREFLY_TYPE, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS,
                Heightmap.Type.MOTION_BLOCKING, FireflyEntity::canSpawnOn);
    }
}