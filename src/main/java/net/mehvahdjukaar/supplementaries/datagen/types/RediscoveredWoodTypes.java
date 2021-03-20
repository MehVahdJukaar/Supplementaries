package net.mehvahdjukaar.supplementaries.datagen.types;

public enum RediscoveredWoodTypes implements IWoodType {
    CHERRY("cherry");

    private final String name;

    RediscoveredWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_red";
    }

    @Override
    public String getNamespace() {
        return "rediscovered";
    }
}
