package net.mehvahdjukaar.supplementaries.datagen.types;


import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


public enum ForbiddenArcanusWoodTypes implements IWoodType {
    ARCANE_EDELWOOD("arcane_edelwood"),
    CHERRYWOOD("cherrywood"),
    MYSTERYWOOD("mysterywood"),
    EDELWOOD("edelwood");

    private final String name;
    private final MaterialColor color;
    private final Material material;

    ForbiddenArcanusWoodTypes(String name) {
        this.name = name;
        this.color = Blocks.OAK_PLANKS.getMaterialColor();
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
        return "forbidden_arcanus";
    }
}
