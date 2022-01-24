package net.mehvahdjukaar.supplementaries.datagen.types;

public enum BiomemakeoverWoodTypes implements IWoodType {
    ANCIENT_OAK("ancient_oak"),
    SWAMP_CYPRESS("swamp_cypress"),
    WILLOW("willow"),
    BLIGHTED_BALSA("blighted_balsa");

    private final String name;

    BiomemakeoverWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getRegName() {
        return this.name+"_bm";
    }

    @Override
    public String getNamespace() {
        return "biomemakeover";
    }
}
