package net.mehvahdjukaar.supplementaries.datagen.types;

public enum DruidcraftWoodTypes implements IWoodType {
    ELDER("elder"),
    DARKWOOD("darkwood");

    private final String name;

    DruidcraftWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "druidcraft";
    }
}
