package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;

public enum MalumWoodTypes implements IWoodType {
    RUNEWOOD("runewood");

    private final String name;

    MalumWoodTypes(String name) {
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
