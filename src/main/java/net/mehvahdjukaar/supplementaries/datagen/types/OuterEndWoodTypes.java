package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum OuterEndWoodTypes implements IWoodType {
    AZURE("azure");

    private final String name;

    OuterEndWoodTypes(String name) {
        this.name = name;
    }

    OuterEndWoodTypes(String name, MaterialColor color, Material material) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "outer_end";
    }
}
