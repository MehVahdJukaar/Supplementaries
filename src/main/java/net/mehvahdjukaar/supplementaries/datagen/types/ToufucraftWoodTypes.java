package net.mehvahdjukaar.supplementaries.datagen.types;

public enum ToufucraftWoodTypes implements IWoodType {
    TOFUSTEM("tofustem");

    private final String name;

    ToufucraftWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "tofucraft";
    }
}
