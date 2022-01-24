package net.mehvahdjukaar.supplementaries.datagen.types;

public enum AbundanceWoodTypes implements IWoodType {
    JACARANDA("jacaranda"),
    REDBUD("redbud");

    private final String name;

    AbundanceWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_ab";
    }

    @Override
    public String getNamespace() {
        return "abundance";
    }
}
