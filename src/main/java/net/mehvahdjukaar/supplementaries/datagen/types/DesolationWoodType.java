package net.mehvahdjukaar.supplementaries.datagen.types;

public enum DesolationWoodType implements IWoodType {
    CHARRED("charred");

    private final String name;

    DesolationWoodType(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_de";
    }

    @Override
    public String getNamespace() {
        return "desolation";
    }
}
