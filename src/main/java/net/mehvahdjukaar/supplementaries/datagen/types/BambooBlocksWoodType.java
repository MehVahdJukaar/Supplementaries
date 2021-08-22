package net.mehvahdjukaar.supplementaries.datagen.types;

public enum BambooBlocksWoodType implements IWoodType {
    BAMBOO("bamboo");

    private final String name;

    BambooBlocksWoodType(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "bamboo_blocks";
    }
}
