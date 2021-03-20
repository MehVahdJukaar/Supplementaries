package net.mehvahdjukaar.supplementaries.datagen.types;

public enum MysticalWorldWoodTypes implements IWoodType {
    CHARRED("charred");

    private final String name;

    MysticalWorldWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "mysticalworld";
    }
}
