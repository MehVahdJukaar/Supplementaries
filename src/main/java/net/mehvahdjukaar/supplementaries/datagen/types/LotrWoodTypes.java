package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.material.MaterialColor;

public enum LotrWoodTypes implements IWoodType {
    PINE("pine",MaterialColor.SAND),
    MALLORON("mallorn",MaterialColor.QUARTZ),
    MIRK_OAK("mirk_oak",MaterialColor.COLOR_BROWN),
    CHARRED("charred",MaterialColor.COLOR_BLACK),
    APPLE("apple",MaterialColor.WOOD),
    PEAR("pear",MaterialColor.WOOD),
    CHERRY("cherry",MaterialColor.TERRACOTTA_RED),
    LEBETHORN("lebethron",MaterialColor.COLOR_BROWN),
    BEECH("beech",MaterialColor.WOOD),
    MAPLE("maple",MaterialColor.WOOD),
    ASPEN("aspen",MaterialColor.PODZOL),
    LAIRELOSSE("lairelosse",MaterialColor.SNOW),
    CEDAR("cedar",MaterialColor.COLOR_BROWN),
    FIR("fir",MaterialColor.WOOD),
    LARCH("larch",MaterialColor.PODZOL),
    HOLLY("holly",MaterialColor.SAND),
    GREEN_OAK("green_oak",MaterialColor.COLOR_BROWN),
    CYPRESS("cypress",MaterialColor.SAND),
    ROTTEN("rotten",MaterialColor.PODZOL);


    private final String name;
    private final MaterialColor color;

    LotrWoodTypes(String name, MaterialColor color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public MaterialColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_lotr";
    }

    @Override
    public String getNamespace() {
        return "lotr";
    }
}
