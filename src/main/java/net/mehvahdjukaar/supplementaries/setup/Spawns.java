package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
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
                ResourceLocation biomres = ForgeRegistries.BIOMES.getKey(biome);
                boolean biomeCriteria = false;
                if (biomres.equals(new ResourceLocation("plains")))
                    biomeCriteria = true;
                if (biomres.equals(new ResourceLocation("swamp")))
                    biomeCriteria = true;
                if (biomres.equals(new ResourceLocation("sunflower_plains")))
                    biomeCriteria = true;
                if (biomres.equals(new ResourceLocation("dark_forest")))
                    biomeCriteria = true;
                if (biomres.equals(new ResourceLocation("dark_forest_hills")))
                    biomeCriteria = true;


                //TODO:adjust this so they can spawn on more blocks but not underground
                if(biomeCriteria) {
                    event.getSpawns().getSpawner(EntityClassification.AMBIENT).add(new MobSpawnInfo.Spawners(Registry.FIREFLY_TYPE, 2, 4, 7));
                }

            }
        }
    }
}