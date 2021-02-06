package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum SimplyTeadWoodTypes implements IWoodType {
    TEA("tea");

    private final String name;
    private final MaterialColor color;
    private final Material material;

    SimplyTeadWoodTypes(String name) {
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
    public String getNamespace() {
        return "simplytea";
    }
}
