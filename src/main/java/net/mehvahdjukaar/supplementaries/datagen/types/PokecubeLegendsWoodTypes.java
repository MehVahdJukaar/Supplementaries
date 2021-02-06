package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum PokecubeLegendsWoodTypes implements IWoodType {
    INVERTED("inverted","ultra_plank01"),
    TEMPORAL("inverted","ultra_plank02"),
    AGED("inverted","ultra_plank03"),
    DISTORTIC("distortic","distortic_plank");

    private final String name;
    private final String plank;
    private final MaterialColor color;
    private final Material material;

    PokecubeLegendsWoodTypes(String name, String plank) {
        this.name = name;
        this.color = Blocks.OAK_PLANKS.getMaterialColor();
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
        this.plank = plank;
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
        return "pokecube_legends";
    }

    @Override
    public String getPlankRegName() {
        return this.getNamespace()+":"+this.plank;
    }
}
