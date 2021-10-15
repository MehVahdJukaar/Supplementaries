package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;


public enum SimplyTeadWoodTypes implements IWoodType {
    TEA("tea");

    private final String name;
    private final MaterialColor color;

    SimplyTeadWoodTypes(String name) {
        this.name = name;
        this.color = MaterialColor.COLOR_BROWN;
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
        return "simplytea";
    }
}
