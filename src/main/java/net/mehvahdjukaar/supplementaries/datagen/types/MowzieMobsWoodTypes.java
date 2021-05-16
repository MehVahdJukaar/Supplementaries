package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;

public enum MowzieMobsWoodTypes implements IWoodType {
    PAINTED_ACACIA("painted_acacia", MaterialColor.COLOR_ORANGE);

    private final String name;
    private final MaterialColor color;

    MowzieMobsWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
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
    public String getPlankRegName() {
        return this.getNamespace()+":"+this.toString();
    }

    @Override
    public String getNamespace() {
        return "mowziesmobs";
    }
}
