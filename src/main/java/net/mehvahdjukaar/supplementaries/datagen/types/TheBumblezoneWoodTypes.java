package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;


public enum TheBumblezoneWoodTypes implements IWoodType {
    BEESWAX_PLANKS("beeswax",MaterialColor.COLOR_YELLOW);

    private final String name;
    private final MaterialColor color;

    TheBumblezoneWoodTypes(String name,MaterialColor color) {
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
        return "the_bumblezone";
    }
}
