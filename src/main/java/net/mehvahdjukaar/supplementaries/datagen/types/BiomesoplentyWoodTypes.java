package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;


public enum BiomesoplentyWoodTypes implements IWoodType {
    FIR("fir",MaterialColor.WHITE_TERRACOTTA),
    REDWOOD("redwood",MaterialColor.RED),
    CHERRY("cherry",MaterialColor.RED),
    MAHOGANY("mahogany",MaterialColor.PINK_TERRACOTTA),
    JACARANDA("jacaranda",MaterialColor.WHITE_TERRACOTTA),
    PALM("palm",MaterialColor.YELLOW_TERRACOTTA),
    WILLOW("willow",MaterialColor.LIME_TERRACOTTA),
    DEAD("dead",MaterialColor.STONE),
    MAGIC("magic",MaterialColor.BLUE),
    UMBRAN("umbran",MaterialColor.BLUE_TERRACOTTA),
    HELLBARK("hellbark",MaterialColor.GRAY_TERRACOTTA);

    private final String name;
    private final MaterialColor color;

    BiomesoplentyWoodTypes(String name, MaterialColor color) {
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
    public String getRegName() {
        return this.name+"_bop";
    }

    @Override
    public String getNamespace() {
        return "biomesoplenty";
    }
}
