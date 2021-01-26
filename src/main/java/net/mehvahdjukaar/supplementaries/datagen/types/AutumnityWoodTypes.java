package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum AutumnityWoodTypes implements IWoodType {
    MAPLE("maple", MaterialColor.ORANGE_TERRACOTTA, Material.WOOD);

    private final String name;
    private final MaterialColor color;
    private final Material material;

    AutumnityWoodTypes(String name) {
        this.name = name;
        this.color = Blocks.OAK_PLANKS.getMaterialColor();
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
    }

    AutumnityWoodTypes(String name, MaterialColor color, Material material) {
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
        return "autumnity";
    }
}
