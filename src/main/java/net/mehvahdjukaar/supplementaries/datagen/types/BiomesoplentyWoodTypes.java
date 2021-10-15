package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;


public enum BiomesoplentyWoodTypes implements IWoodType {
    FIR("fir",MaterialColor.TERRACOTTA_WHITE),
    REDWOOD("redwood",MaterialColor.COLOR_RED),
    CHERRY("cherry",MaterialColor.COLOR_RED),
    MAHOGANY("mahogany",MaterialColor.TERRACOTTA_PINK),
    JACARANDA("jacaranda",MaterialColor.TERRACOTTA_WHITE),
    PALM("palm",MaterialColor.TERRACOTTA_YELLOW),
    WILLOW("willow",MaterialColor.TERRACOTTA_LIGHT_GREEN),
    DEAD("dead",MaterialColor.STONE),
    MAGIC("magic",MaterialColor.COLOR_BLUE),
    UMBRAN("umbran",MaterialColor.TERRACOTTA_BLUE),
    HELLBARK("hellbark",MaterialColor.TERRACOTTA_GRAY);

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
