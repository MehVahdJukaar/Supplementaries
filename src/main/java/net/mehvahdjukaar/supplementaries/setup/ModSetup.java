package net.mehvahdjukaar.supplementaries.setup;


import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.DefaultFlowersFeature;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.Random;

public class ModSetup {

    public static void init(final FMLClientSetupEvent event) {

        Dispenser.registerBehaviors();
        GlobalEntityTypeAttributes.put(Registry.FIREFLY.get(), FireflyEntity.setCustomAttributes().create());
        //DeferredWorkQueue.runLater(()->{ });
        registerSpawningStuff();

    }

    private static void registerSpawningStuff(){
        //TODO:adjust this so they can spawn on more blocks but not underground
        EntitySpawnPlacementRegistry.register(Registry.FIREFLY.get(), EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS,
                Heightmap.Type.WORLD_SURFACE, FireflyEntity::canSpawnOn);
    }

    public static void reflectionStuff(){
        Method[] methods = FireBlock.class.getMethods();
        // get the name of every method present in the list
        for (Method method : methods) {
            String MethodName = method.getName();
            System.out.println("Name of the method: "
                    + MethodName);
        }
    }




}
