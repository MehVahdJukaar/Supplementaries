package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public enum UndergardenWoodTypes implements IWoodType {
    GRONGLE("grongle", MaterialColor.CRIMSON_STEM, Material.NETHER_WOOD),
    SMOGSTEM("smogstem", MaterialColor.WOOD,Material.WOOD),
    WIGGLE_WOOD("wigglewood", MaterialColor.WOOD,Material.WOOD);

    private final String name;
    private final MaterialColor color;
    private final Material material;

    UndergardenWoodTypes(String name, MaterialColor color,Material material) {
        this.name = name;
        this.material = material;
        this.color = color;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public MaterialColor getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "undergarden";
    }
}
