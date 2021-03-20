package net.mehvahdjukaar.supplementaries.datagen.types;

public enum StructurizeWoodTypes implements IWoodType {
    CACTUS("cactus");

    private final String name;

    StructurizeWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getPlankRegName() {
        return "blockcactusplank";
    }

    @Override
    public String getNamespace() {
        return "structurize";
    }
}
