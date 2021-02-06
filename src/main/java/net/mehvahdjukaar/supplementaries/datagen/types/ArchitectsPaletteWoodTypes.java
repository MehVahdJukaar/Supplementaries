package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum ArchitectsPaletteWoodTypes implements IWoodType {
    TWISTED("twisted",MaterialColor.PURPLE);

    private final String name;
    private final MaterialColor color;
    private final Material material;

    ArchitectsPaletteWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
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
        return "architects_palette";
    }
}
