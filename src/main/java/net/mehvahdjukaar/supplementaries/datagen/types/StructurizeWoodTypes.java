package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum StructurizeWoodTypes implements IWoodType {
    CACTUS("cactus");

    private final String name;
    private final MaterialColor color;
    private final Material material;

    StructurizeWoodTypes(String name) {
        this.name = name;
        this.color = MaterialColor.BROWN;
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
    public String getPlankRegName() {
        return "blockcactusplank";
    }

    @Override
    public String getNamespace() {
        return "structurize";
    }
}
