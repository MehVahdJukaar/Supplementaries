package net.mehvahdjukaar.supplementaries.datagen.types;

public enum TerraqueousWoodTypes implements IWoodType {
    BANANA("banana"),
    CHERRY("cherry"),
    COCONUT("coconut"),
    PEAR("pear"),
    MANGO("mango"),
    MULBERRY("mulberry"),
    ORANGE("orange"),
    PEACH("peach"),
    PLUM("plum"),
    APPLE("apple"),
    LEMON("lemon");

    private final String name;

    TerraqueousWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.toString()+"_ter";
    }

    @Override
    public String getNamespace() {
        return "terraqueous";
    }
}
