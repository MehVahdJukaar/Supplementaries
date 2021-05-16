package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;

public enum TwilightForestWoodTypes implements IWoodType {
    CANOPY("canopy", MaterialColor.PODZOL),
    DARK("dark",MaterialColor.COLOR_ORANGE),
    MANGROVE("mangrove",MaterialColor.DIRT),
    MINE("mine",MaterialColor.SAND),
    SORT("sort",MaterialColor.PODZOL),
    TIME("time",MaterialColor.DIRT),
    TRANS("trans",MaterialColor.WOOD),
    TWILIGHT_OAK("twilight_oak",MaterialColor.WOOD);

    private final String name;
    private final MaterialColor color;

    TwilightForestWoodTypes(String name, MaterialColor color) {
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
        return this.name+"_tf";
    }

    @Override
    public String getNamespace() {
        return "twilightforest";
    }
}
