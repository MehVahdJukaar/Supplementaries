package net.mehvahdjukaar.supplementaries.datagen.types;


public enum BayouBluesWoodType implements IWoodType {
    CYPRESS("cypress");

    private final String name;

    BayouBluesWoodType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_bb";
    }

    @Override
    public String getNamespace() {
        return "bayou_blues";
    }
}
