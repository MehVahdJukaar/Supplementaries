package net.mehvahdjukaar.supplementaries.datagen.types;

public enum PokecubeLegendsWoodTypes implements IWoodType {
    INVERTED("inverted","ultra_plank01"),
    TEMPORAL("temporal","ultra_plank02"),
    AGED("aged","ultra_plank03"),
    DISTORTIC("distortic","distortic_plank");

    private final String name;
    private final String plank;

    PokecubeLegendsWoodTypes(String name, String plank) {
        this.name = name;
        this.plank = plank;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "pokecube_legends";
    }

    @Override
    public String getPlankRegName() {
        return this.getNamespace()+":"+this.plank;
    }
}
