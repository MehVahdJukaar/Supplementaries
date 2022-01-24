package net.mehvahdjukaar.supplementaries.datagen.types;


public enum UnnamedAnimalModWoodTypes implements IWoodType {
    MANGROVE("mangrove");

    private final String name;

    UnnamedAnimalModWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_un";
    }

    @Override
    public String getNamespace() {
        return "unnamedanimalmod";
    }
}
