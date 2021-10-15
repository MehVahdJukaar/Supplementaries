package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;

public enum AtumWoodType implements IWoodType {
    PALM("palm"),
    DEADWOOD("deadwood");

    private final String name;

    AtumWoodType(String name) {
        this.name = name;
    }

    @Override
    public MaterialColor getColor() {
        return this==PALM?MaterialColor.WOOD:MaterialColor.PODZOL;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_atum";
    }

    @Override
    public String getNamespace() {
        return "atum";
    }
}
