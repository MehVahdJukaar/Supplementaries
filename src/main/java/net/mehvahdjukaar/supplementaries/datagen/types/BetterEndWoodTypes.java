package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;


public enum BetterEndWoodTypes implements IWoodType {
    MOSSY_GLOWSHROOM ("mossy_glowshroom", MaterialColor.WOOD),
    LACUGROVE ("lacugrove", MaterialColor.COLOR_YELLOW),
    END_LOTUS ("end_lotus",  MaterialColor.COLOR_CYAN),
    PYTHADENDRON ("pythadendron",  MaterialColor.COLOR_PURPLE),
    DRAGON_TREE ("dragon_tree", MaterialColor.COLOR_MAGENTA),
    TENANEA ("tenanea", MaterialColor.COLOR_PINK),
    HELIX_TREE ("helix_tree", MaterialColor.COLOR_ORANGE),
    UMBRELLA_TREE ("umbrella_tree",  MaterialColor.COLOR_GREEN),
    JELLYSHROOM ("jellyshroom",  MaterialColor.COLOR_LIGHT_BLUE);

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
