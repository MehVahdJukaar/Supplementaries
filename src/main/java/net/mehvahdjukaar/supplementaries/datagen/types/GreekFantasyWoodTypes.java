package net.mehvahdjukaar.supplementaries.datagen.types;

public enum GreekFantasyWoodTypes implements IWoodType {
    OLIVE("olive");

    private final String name;

    GreekFantasyWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "greekfantasy";
    }
}
