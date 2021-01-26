package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum OuterEndWoodTypes implements IWoodType {
    AZURE("azure", MaterialColor.CYAN, Material.WOOD);

    private final String name;
    private final MaterialColor color;
    private final Material material;

    OuterEndWoodTypes(String name) {
        this.name = name;
        this.color = Blocks.OAK_PLANKS.getMaterialColor();
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
    }

    OuterEndWoodTypes(String name, MaterialColor color, Material material) {
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
        return "outer_end";
    }
}
