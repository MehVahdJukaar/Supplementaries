package net.mehvahdjukaar.supplementaries.datagen.types;

public enum GoodNightSleepWoodTypes implements IWoodType {
    BLOOD("blood"),
    DEAD("dead"),
    DREAM("dream"),
    WHITE("white");

    private final String name;

    GoodNightSleepWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "good_nights_sleep";
    }
}
