package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;


public enum TraverseWoodTypes implements IWoodType {
    FIR("fir",MaterialColor.BROWN);

    private final String name;
    private final MaterialColor color;

    TraverseWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public MaterialColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_tr";
    }

    @Override
    public String getNamespace() {
        return "traverse";
    }
}
