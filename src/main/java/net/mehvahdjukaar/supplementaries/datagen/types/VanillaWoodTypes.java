package net.mehvahdjukaar.supplementaries.datagen.types;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public enum VanillaWoodTypes implements IWoodType {
    OAK("oak", Blocks.OAK_PLANKS),
    BIRCH("birch", Blocks.BIRCH_PLANKS),
    SPRUCE("spruce", Blocks.SPRUCE_PLANKS),
    JUNGLE("jungle", Blocks.JUNGLE_PLANKS),
    ACACIA("acacia", Blocks.ACACIA_PLANKS),
    DARK_OAK("dark_oak", Blocks.DARK_OAK_PLANKS),
    CRIMSON("crimson", Blocks.CRIMSON_PLANKS),
    WARPED("warped", Blocks.WARPED_PLANKS);

    private final String name;
    private final MaterialColor color;
    private final Material material;


    VanillaWoodTypes(String name, Block plank) {
        this.name = name;
        this.color = plank.getMaterialColor();
        this.material = plank.getDefaultState().getMaterial();

    }

    VanillaWoodTypes(String name, MaterialColor color, Material material) {
        this.name = name;
        this.color = color;
        this.material = material;
    }
    VanillaWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
        this.material = Material.WOOD;
    }
    VanillaWoodTypes(String name) {
        this.name = name;
        this.color = MaterialColor.WOOD;
        this.material = Material.WOOD;
    }


    @Override
    public MaterialColor getColor() {
        return this.color;
    }

    @Override
    public Material getMaterial() {
        return this.material;
    }

    public String toString() {
        return this.getName();
    }

    public String getName() {
        return this.name;
    }
}
