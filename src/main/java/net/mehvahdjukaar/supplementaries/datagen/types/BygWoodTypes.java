package net.mehvahdjukaar.supplementaries.datagen.types;


import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum BygWoodTypes implements IWoodType {
    WILLOW("willow", MaterialColor.GREEN_TERRACOTTA, Material.WOOD),
    WITCH_HAZEL("witch_hazel", MaterialColor.GRASS, Material.WOOD);

    private final String name;
    private final MaterialColor color;
    private final Material material;

    BygWoodTypes(String name) {
        this.name = name;
        this.color = Blocks.OAK_PLANKS.getMaterialColor();
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
    }

    BygWoodTypes(String name, MaterialColor color, Material material) {
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
        return "byg";
    }
}
