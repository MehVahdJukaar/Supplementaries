package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;


public enum ArchitectsPaletteWoodTypes implements IWoodType {
    TWISTED("twisted",MaterialColor.COLOR_PURPLE);

    private final String name;
    private final MaterialColor color;

    ArchitectsPaletteWoodTypes(String name, MaterialColor color) {
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
    public String getNamespace() {
        return "architects_palette";
    }
}
