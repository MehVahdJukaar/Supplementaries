package net.mehvahdjukaar.supplementaries.datagen.types;

public enum EnhancedMushroomsWoodTypes implements IWoodType {
    BROWN_MUSHROOM("brown_mushroom"),
    RED_MUSHROOM("red_mushroom"),
    GLOWSHROOM("glowshroom");

    private final String name;

    EnhancedMushroomsWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "enhanced_mushrooms";
    }
}
