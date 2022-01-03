package net.mehvahdjukaar.supplementaries.datagen.types;

public enum MorecraftWoodTypes implements IWoodType {
    NETHERWOOD("netherwood");

    private final String name;

    MorecraftWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "morecraft";
    }

    @Override
    public String getRegName() {
        return this.name+"_mc";
    }
}
