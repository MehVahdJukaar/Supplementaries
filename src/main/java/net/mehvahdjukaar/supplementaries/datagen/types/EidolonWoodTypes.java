package net.mehvahdjukaar.supplementaries.datagen.types;


public enum EidolonWoodTypes implements IWoodType {
    POLISHED("polished");

    private final String name;

    EidolonWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "eidolon";
    }
}
