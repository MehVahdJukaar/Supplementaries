package net.mehvahdjukaar.supplementaries.datagen.types;


import net.minecraft.block.material.MaterialColor;


public enum EndergeticWoodTypes implements IWoodType {
    POISE("poise", MaterialColor.PURPLE_TERRACOTTA);

    private final String name;
    private final MaterialColor color;

    EndergeticWoodTypes(String name, MaterialColor color) {
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
        return "endergetic";
    }
}
