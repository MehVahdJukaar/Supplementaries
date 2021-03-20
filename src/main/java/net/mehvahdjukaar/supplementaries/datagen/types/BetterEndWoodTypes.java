package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;


public enum BetterEndWoodTypes implements IWoodType {
    MOSSY_GLOWSHROOM ("mossy_glowshroom", MaterialColor.WOOD),
    LACUGROVE ("lacugrove", MaterialColor.YELLOW),
    END_LOTUS ("end_lotus",  MaterialColor.CYAN),
    PYTHADENDRON ("pythadendron",  MaterialColor.PURPLE),
    DRAGON_TREE ("dragon_tree", MaterialColor.MAGENTA),
    TENANEA ("tenanea", MaterialColor.PINK),
    HELIX_TREE ("helix_tree", MaterialColor.ADOBE),
    UMBRELLA_TREE ("umbrella_tree",  MaterialColor.GREEN),
    JELLYSHROOM ("jellyshroom",  MaterialColor.LIGHT_BLUE);

    private final String name;
    private final MaterialColor color;

    BetterEndWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public MaterialColor getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "betterendforge";
    }
}
