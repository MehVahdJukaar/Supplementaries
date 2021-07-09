package net.mehvahdjukaar.supplementaries.datagen.types;

public enum ArsNouveauWoodType implements IWoodType {
    ARCHWOOD("archwood");

    private final String name;

    ArsNouveauWoodType(String name) {
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
        return "ars_nouveau";
    }
}
