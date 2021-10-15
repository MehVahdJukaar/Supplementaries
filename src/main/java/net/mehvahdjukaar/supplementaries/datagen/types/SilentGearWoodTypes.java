package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;


public enum SilentGearWoodTypes implements IWoodType {
    NETHERWOOD("netherwood");

    private final String name;
    private final MaterialColor color;
    private final Material material;

    SilentGearWoodTypes(String name) {
        this.name = name;
        this.color = Blocks.CRIMSON_PLANKS.defaultMaterialColor();
        this.material = Blocks.CRIMSON_PLANKS.defaultBlockState().getMaterial();
    }

    @Override
    public MaterialColor getColor() {
        return this.color;
    }

    @Override
    public Material getMaterial() {
        return this.material;
    }

    @Override
    public String toString() {
        return this.name;
    }
    

    @Override
    public String getNamespace() {
        return "silentgear";
    }
}
