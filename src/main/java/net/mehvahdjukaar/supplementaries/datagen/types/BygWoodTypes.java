package net.mehvahdjukaar.supplementaries.datagen.types;


public enum BygWoodTypes implements IWoodType {
    ASPENS("aspen"),
    BAOBABS("baobab"),
    BLUE_ENCHANTEDS("blue_enchanted"),
    CHERRYS("cherry"),
    CIKAS("cika"),
    CYPRESSS("cypress"),
    EBONYS("ebony"),
    FIRS("fir"),
    GREEN_ENCHANTEDS("green_enchanted"),
    HOLLYS("holly"),
    JACARANDAS("jacaranda"),
    MAHOGANYS("mahogany"),
    MANGROVES("mangrove"),
    MAPLES("maple"),
    PINES("pine"),
    RAINBOW_EUCALYPTUSS("rainbow_eucalyptus"),
    REDWOODS("redwood"),
    SKYRISS("skyris"),
    WILLOWS("willow"),
    WITCH_HAZELS("witch_hazel"),
    ZELKOVAS("zelkova"),
    SYTHIANS("sythian"),
    EMBURS("embur"),
    PALMS("palm"),
    LAMENTS("lament"),
    BULBISS("bulbis"),
    NIGHTSHADES("nightshade"),
    ETHERS("ether");

    private final String name;


    BygWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "byg";
    }
}
