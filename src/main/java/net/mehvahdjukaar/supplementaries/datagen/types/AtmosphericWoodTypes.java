package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum AtmosphericWoodTypes implements IWoodType {
    ASPEN("aspen"),
    GRIMWOOD("grimwood"),
    KOUSA("kousa"),
    MORADO("morado"),
    ROSEWOOD("rosewood"),
    YUCCA("yucca");

    private final String name;
    private final MaterialColor color;
    private final Material material;

    AtmosphericWoodTypes(String name) {
        this.name = name;
        this.color = Blocks.OAK_PLANKS.getMaterialColor();
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
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
        return "atmospheric";
    }
}
