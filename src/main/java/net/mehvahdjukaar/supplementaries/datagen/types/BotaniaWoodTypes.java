package net.mehvahdjukaar.supplementaries.datagen.types;

public enum BotaniaWoodTypes implements IWoodType {
    LIVINGWOOD("livingwood"),
    SHIMMERWOOD("shimmerwood"),
    DREAMWOOD("dreamwood");

    private final String name;

    BotaniaWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "botania";
    }
}
