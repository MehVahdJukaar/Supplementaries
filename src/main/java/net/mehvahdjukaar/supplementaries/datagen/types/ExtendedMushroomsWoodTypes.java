package net.mehvahdjukaar.supplementaries.datagen.types;

public enum ExtendedMushroomsWoodTypes implements IWoodType {
    POISONOUS_MUSHROOM("poisonous_mushroom"),
    MUSHROOM("mushroom"),
    GLOWSHROOM("glowshroom");

    private final String name;

    ExtendedMushroomsWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String getRegName() {
        return this.toString()+"_em";
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "extendedmushrooms";
    }
}
