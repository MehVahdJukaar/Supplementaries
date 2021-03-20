package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.MaterialColor;


public enum PokecubeWoodTypes implements IWoodType {
    ENIGMA("enigma",MaterialColor.BLACK),
    LEPPA("leppa",MaterialColor.MAGENTA),
    NANAB("nanab",MaterialColor.BROWN_TERRACOTTA),
    ORAN("oran",MaterialColor.LIGHT_BLUE),
    PECHA("pecha",MaterialColor.PINK),
    SITRUS("sitrus",MaterialColor.YELLOW_TERRACOTTA);


    private final String name;
    private final MaterialColor color;

    PokecubeWoodTypes(String name, MaterialColor color) {
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
        return "pokecube";
    }

    @Override
    public String getSignRegName() {
        return this.getNamespace()+":"+this.toString()+"_sign";
    }

    @Override
    public String getPlankRegName() {
        return this.getNamespace()+":plank_"+this.toString();
    }
}
