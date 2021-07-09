package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;

public enum HabitatWoodType implements IWoodType {
    FAIRY_RING_MUSHROOM("fairy_ring_mushroom");

    private final String name;

    HabitatWoodType(String name) {
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
        return this.name+"_hbt";
    }

    @Override
    public String getNamespace() {
        return "habitat";
    }
}
