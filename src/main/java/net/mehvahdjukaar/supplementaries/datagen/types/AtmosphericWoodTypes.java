package net.mehvahdjukaar.supplementaries.datagen.types;

public enum AtmosphericWoodTypes implements IWoodType {
    ASPEN("aspen"),
    GRIMWOOD("grimwood"),
    KOUSA("kousa"),
    MORADO("morado"),
    ROSEWOOD("rosewood"),
    YUCCA("yucca");

    private final String name;

    AtmosphericWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        if(this.name.equals("aspen"))return this.name+"_atm";
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "atmospheric";
    }
}
