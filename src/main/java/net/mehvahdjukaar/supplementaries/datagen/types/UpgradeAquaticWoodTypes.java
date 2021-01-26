package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum UpgradeAquaticWoodTypes implements IWoodType {
    DRIFTWOOD("driftwood", MaterialColor.STONE, Material.WOOD),
    RIVERWOOD("river", MaterialColor.BROWN, Material.WOOD);

    private final String name;
    private final MaterialColor color;
    private final Material material;

    UpgradeAquaticWoodTypes(String name) {
        this.name = name;
        this.color = MaterialColor.WOOD;
        this.material = Material.WOOD;
    }

    UpgradeAquaticWoodTypes(String name, MaterialColor color, Material material) {
        this.name = name;
        this.color = color;
        this.material = material;
    }

    @Override
    public MaterialColor getColor() {
        return this.color;
    }

    @Override
    public Material getMaterial() {
        return this.material;
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
