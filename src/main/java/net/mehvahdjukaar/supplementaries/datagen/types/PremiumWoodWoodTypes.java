package net.mehvahdjukaar.supplementaries.datagen.types;

public enum PremiumWoodWoodTypes implements IWoodType {
    MAGIC("magic"),
    MAPLE("maple"),
    PURPLE_HEART("purple_heart"),
    SILVERBELL("silverbell"),
    TIGER("tiger"),
    WILLOW("willow");


    private final String name;

    PremiumWoodWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_pw";
    }

    @Override
    public String getNamespace() {
        return "premium_wood";
    }
}
