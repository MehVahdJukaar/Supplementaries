package net.mehvahdjukaar.supplementaries.datagen.types;

public enum DomumOrnamentumWoodTypes implements IWoodType {
    CACTUS("cactus_extra");

    private final String name;

    DomumOrnamentumWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getPlankRegName() {
        return "cactus_extra";
    }

    @Override
    public String getNamespace() {
        return "domum_ornamentum";
    }
}
