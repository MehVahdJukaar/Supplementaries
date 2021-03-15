package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum EnvironmentalWoodTypes implements IWoodType {
    WILLOW("willow"),
    WISTERIA("wisteria"),
    CHERRY("cherry");

    private final String name;
    private final MaterialColor color;
    private final Material material;

    EnvironmentalWoodTypes(String name) {
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
    public String getRegName() {
        return this.name+"_env";
    }

    @Override
    public String getNamespace() {
        return "environmental";
    }
}
