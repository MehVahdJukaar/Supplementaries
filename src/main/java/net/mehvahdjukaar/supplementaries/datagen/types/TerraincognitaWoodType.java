package net.mehvahdjukaar.supplementaries.datagen.types;

public enum TerraincognitaWoodType implements IWoodType {
    //HAZEL("hazel"),
    APPLE("apple");

    private final String name;

    TerraincognitaWoodType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_te";
    }

    @Override
    public String getNamespace() {
        return "terraincognita";
    }
}
