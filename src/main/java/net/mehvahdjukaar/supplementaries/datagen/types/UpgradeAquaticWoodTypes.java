package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;


public enum UpgradeAquaticWoodTypes implements IWoodType {
    DRIFTWOOD("driftwood", MaterialColor.STONE),
    RIVERWOOD("river", MaterialColor.COLOR_BROWN);

    private final String name;
    private final MaterialColor color;

    UpgradeAquaticWoodTypes(String name, MaterialColor color) {
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
        return "upgrade_aquatic";
    }
}
