package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;

public enum MalumWoodType implements IWoodType {
    RUNEWOOD("runewood");

    private final String name;

    MalumWoodType(String name) {
        this.name = name;
    }

    @Override
    public MaterialColor getColor() {
        return MaterialColor.COLOR_YELLOW;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "malum";
    }
}
