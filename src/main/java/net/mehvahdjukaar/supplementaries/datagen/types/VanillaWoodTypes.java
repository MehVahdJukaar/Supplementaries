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
        this.color = plank.defaultMaterialColor();
        this.material = plank.defaultBlockState().getMaterial();
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
    public String getLocation() {
        return "";
    }

    @Override
    public String getNamespace() {
        return "minecraft";
    }

}
