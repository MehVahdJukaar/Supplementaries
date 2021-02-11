package net.mehvahdjukaar.supplementaries.datagen.types;



import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;


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
    private final MaterialColor color;
    private final Material material;


    BygWoodTypes(String name) {
        this.name = name;
        this.color = MaterialColor.BROWN;
        this.material = Blocks.OAK_PLANKS.getDefaultState().getMaterial();
    }

    @Override
    public MaterialColor getColor() {
        return this.color;
    }

    @Override
    public Material getMaterial() {
        return this.material;
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
