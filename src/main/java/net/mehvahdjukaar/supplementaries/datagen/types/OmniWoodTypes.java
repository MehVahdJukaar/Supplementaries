package net.mehvahdjukaar.supplementaries.datagen.types;

public enum OmniWoodTypes implements IWoodType {
    CAVE_MUSHROOM("cave_mushroom");

    private final String name;

    OmniWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "omni";
    }
}
