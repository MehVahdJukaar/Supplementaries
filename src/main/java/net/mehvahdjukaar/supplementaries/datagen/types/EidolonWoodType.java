package net.mehvahdjukaar.supplementaries.datagen.types;


public enum EidolonWoodType implements IWoodType {
    POLISHED("polished");

    private final String name;

    EidolonWoodType(String name) {
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
