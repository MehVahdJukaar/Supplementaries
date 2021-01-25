package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Block;
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


    AtmosphericWoodTypes(String name, Block plank) {
        this.name = name;
        this.color = plank.getMaterialColor();
        this.material = plank.getDefaultState().getMaterial();
    }

    AtmosphericWoodTypes(String name, MaterialColor color, Material material) {
        this.name = name;
        this.color = color;
        this.material = material;
    }
    AtmosphericWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
        this.material = Material.WOOD;
    }
    AtmosphericWoodTypes(String name) {
        this.name = name;
        this.color = MaterialColor.WOOD;
        this.material = Material.WOOD;
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
    public String getLocation() {
        return "atmospheric/";
    }
}
