package net.mehvahdjukaar.supplementaries.datagen.types;

public enum EnvironmentalWoodTypes implements IWoodType {
    WILLOW("willow"),
    WISTERIA("wisteria"),
    CHERRY("cherry");

    private final String name;

    EnvironmentalWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_env";
    }

    @Override
    public String getNamespace() {
        return "environmental";
    }
}
