package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.generation.structure.WaySignStructure;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    //structures
    public static final DeferredRegister<StructureFeature<?>> STRUCTURES = DeferredRegister.create(
            ForgeRegistries.STRUCTURE_FEATURES, Supplementaries.MOD_ID);

    public static final String WAY_SIGN_NAME = "way_sign";
    public static final RegistryObject<StructureFeature<JigsawConfiguration>> WAY_SIGN = STRUCTURES.register(
            WAY_SIGN_NAME, WaySignStructure::new);


}

