package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.generation.structure.WaySignStructure;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    //structures
    public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(
            Registry.STRUCTURE_TYPE_REGISTRY, Supplementaries.MOD_ID);

    public static final String WAY_SIGN_NAME = "way_sign";
    public static final RegistryObject<StructureType<?>> WAY_SIGN = STRUCTURES.register(
            WAY_SIGN_NAME, WaySignStructure::new);


}

