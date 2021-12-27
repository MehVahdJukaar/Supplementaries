package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.fluids.FluidTextures;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModSoftFluids {
    public static SoftFluid makeSF(SoftFluid.Builder builder) {
        return new SoftFluid(builder);
    }

    public static final SoftFluid DIRT;
    public static final SoftFluid SAP;
    public static final SoftFluid POWDERED_SNOW;
    public static final SoftFluid UNHOLY_SAP;
    public static final SoftFluid HOLY_SAP;
    //TODO: improve and move to compat handler

    //mod compat fluids
    //TODO: move to data
    static {
        POWDERED_SNOW = makeSF(new SoftFluid.Builder(Textures.POWDER_SNOW_TEXTURE, Textures.POWDER_SNOW_TEXTURE, "powder_snow")
                .fromMod(Supplementaries.MOD_ID)
                .bucket(Items.POWDER_SNOW_BUCKET)
                .setBucketSounds(SoundEvents.BUCKET_FILL_POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW)
                .translationKey("block.minecraft.powder_snow"));
        DIRT = makeSF(new SoftFluid.Builder(Textures.DIRT_TEXTURE, Textures.DIRT_TEXTURE, "dirt")
                .fromMod(Supplementaries.MOD_ID)
                .emptyHandContainerItem(Items.DIRT, 4)
                .setSoundsForCategory(SoundEvents.GRAVEL_PLACE, SoundEvents.GRAVEL_BREAK, Items.AIR)
                .translationKey("block.minecraft.dirt"));
        //need this here so I can reference it
        SAP = makeSF(new SoftFluid.Builder(FluidTextures.HONEY_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW, "sap")
                .fromMod(Supplementaries.MOD_ID)
                .copyTexturesFrom("thermal:sap")
                .color(0xbd6e2a)
                .drink("autumnity:sap_bottle")
                .bucket("thermal:sap_bucket")
                .translationKey("fluid.supplementaries.sap"));

        HOLY_SAP = makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "holy_sap")
                .fromMod("malum")
                .color(0xDB8F47)
                .translationKey("item.malum:holy_sap")
                .bottle("malum:holy_sap"));

        UNHOLY_SAP = makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "unholy_sap")
                .fromMod("malum")
                .color(0x762550)
                .translationKey("item.malum:unholy_sap")
                .drink("malum:unholy_sap"));
    }


    public static void init() {

        SoftFluidRegistry.register(SAP);
        SoftFluidRegistry.register(DIRT);
        SoftFluidRegistry.register(POWDERED_SNOW);
        SoftFluidRegistry.register(UNHOLY_SAP);
        SoftFluidRegistry.register(HOLY_SAP);

        List<SoftFluid> custom = new ArrayList<>(Collections.emptyList());

        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "komodo_spit")
                .fromMod("alexsmobs")
                .color(0xa8b966)
                .translationKey("item.alexmobs.komodo_spit")
                .bottle("alexsmobs:komodo_spit_bottle")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "squash_soup")
                .fromMod("simplefarming")
                .color(0xe6930a)
                .stew("simplefarming:squash_soup")));
        custom.add(makeSF(new SoftFluid.Builder("inspirations:potato_soup")
                .textures(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW)
                .fromMod("inspirations")
                .stew("inspirations:potato_soup")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "fish_oil")
                .fromMod("alexsmobs")
                .color(0xFFE89C)
                .translationKey("item.alexsmobs.fish_oil")
                .drink("alexsmobs:fish_oil")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "poison")
                .fromMod("alexsmobs")
                .color(0x8AEB67)
                .translationKey("item.alexsmobs.poison_bottle")
                .bottle("alexsmobs:poison_bottle")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "sopa_de_macaco")
                .fromMod("alexsmobs")
                .color(0xB6C184)
                .stew("alexsmobs:sopa_de_macaco")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "baked_cod_stew")
                .fromMod("farmersdelight")
                .color(0xECCD96)
                .stew("farmersdelight:baked_cod_stew")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "beef_stew")
                .fromMod("farmersdelight")
                .color(0x713F2D)
                .stew("farmersdelight:beef_stew")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "chicken_soup")
                .fromMod("farmersdelight")
                .color(0xDEA766)
                .stew("farmersdelight:chicken_soup")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "fish_stew")
                .fromMod("farmersdelight")
                .color(0xB34420)
                .stew("farmersdelight:fish_stew")));
        //TODO: add honey and milk flowing textures
        custom.add(makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE, Textures.MILK_TEXTURE, "hot_cocoa")
                .fromMod("farmersdelight")
                //.textureOverride("create:chocolate",0xe98352)
                .color(0x8F563B)
                .translationKey("item.farmersdelight.hot_cocoa")
                .drink("farmersdelight:hot_cocoa")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "pumpkin_soup")
                .fromMod("farmersdelight")
                .color(0xE38A1D)
                .translationKey("item.farmersdelight.pumpkin_soup")
                .stew("farmersdelight:pumpkin_soup")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "tomato_sauce")
                .fromMod("farmersdelight")
                .color(0xC0341F)
                .translationKey("item.farmersdelight.tomato_sauce")
                .stew("farmersdelight:tomato_sauce")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE, Textures.POTION_TEXTURE_FLOW, "syrup")
                .fromMod("autumnity")
                .copyTexturesFrom("create:honey")
                .color(0x8e3f26)
                .addEqFluid("thermal:syrup")
                .translationKey("item.autumnity.syrup")
                .drink("autumnity:syrup_bottle")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "fire_stew")
                .fromMod("iceandfire")
                .color(0xEB5D10)
                .stew("iceandfire:fire_stew")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "frost_stew")
                .fromMod("iceandfire")
                .color(0x81F2F9)
                .stew("iceandfire:frost_stew")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "lightning_stew")
                .fromMod("iceandfire")
                .color(0x7552C2)
                .stew("iceandfire:lightning_stew")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "fire_dragon_blood")
                .fromMod("iceandfire")
                .color(0xEB5D10)
                .translationKey("item.iceandfire.fire_dragon_blood")
                .drink("iceandfire:fire_dragon_blood")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "lightning_dragon_blood")
                .fromMod("iceandfire")
                .color(0xA700FC)
                .translationKey("item.iceandfire.lightning_dragon_blood")
                .drink("iceandfire:lightning_dragon_blood")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "ice_dragon_blood")
                .fromMod("iceandfire")
                .color(0x1BCFFC)
                .translationKey("item.iceandfire.ice_dragon_blood")
                .drink("iceandfire:ice_dragon_blood")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "vegetable_soup")
                .fromMod("farmersdelight")
                .color(0x8A7825)
                .stew("farmersdelight:vegetable_soup")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE, Textures.MILK_TEXTURE, "goat_milk")
                .fromMod("betteranimalsplus")
                .translationKey("item.betteranimalsplus.goatmilk")
                .bucket("betteranimalsplus:goatmilk")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "whiskey")
                .fromMod("simplefarming")
                .color(0xd29062)
                .translationKey("item.simplefarming.whiskey")
                .drink("simplefarming:whiskey")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "olive_oil")
                .fromMod("simplefarming")
                .color(0x969F1C)
                .translationKey("item.simplefarming.olive_oil")
                .drink("simplefarming:olive_oil")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "vinegar")
                .fromMod("simplefarming")
                .color(0xD4D2C4)
                .translationKey("item.simplefarming.vinegar")
                .drink("simplefarming:vinegar")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "mead")
                .fromMod("simplefarming")
                .color(0xC39710)
                .translationKey("item.simplefarming.mead")
                .drink("simplefarming:mead")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "beer")
                .fromMod("simplefarming")
                .color(0xCB9847)
                .translationKey("item.simplefarming.beer")
                .drink("simplefarming:beer")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "cauim")
                .fromMod("simplefarming")
                .color(0xCFC273)
                .translationKey("item.simplefarming.cauim")
                .drink("simplefarming:cauim")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "cider")
                .fromMod("simplefarming")
                .color(0xDC921E)
                .translationKey("item.simplefarming.cider")
                .drink("simplefarming:cider")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "sake")
                .fromMod("simplefarming")
                .color(0xE3D56C)
                .translationKey("item.simplefarming.sake")
                .drink("simplefarming:sake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "tiswin")
                .fromMod("simplefarming")
                .color(0xDC5826)
                .translationKey("item.simplefarming.tiswin")
                .drink("simplefarming:tiswin")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "vodka")
                .fromMod("simplefarming")
                .color(0xCFDFEB)
                .translationKey("item.simplefarming.vodka")
                .drink("simplefarming:vodka")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "wine")
                .fromMod("simplefarming")
                .color(0x961D49)
                .translationKey("item.simplefarming.wine")
                .drink("simplefarming:wine")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE, Textures.HONEY_TEXTURE, "jam")
                .fromMod("simplefarming")
                .color(0x970C1F)
                .translationKey("item.simplefarming.jam")
                .drink("simplefarming:jam")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "umbrella_cluster_juice")
                .fromMod("betterendforge")
                .color(0xBE53F6)
                .translationKey("item.betterendforge.umbrella_cluster_juice")
                .drink("betterendforge:umbrella_cluster_juice")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "soap")
                .fromMod("fluffy_farmer")
                .color(0xdb9eff)
                .translationKey("item.fluffy_farmer.soap")
                .bottle("fluffy_farmer:bottle_with_soap_bubbles")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "soap")
                .fromMod("betteranimalsplus")
                .color(0x60A8E0)
                .translationKey("item.betteranimalsplus.horseshoe_crab_blood")
                .drink("betteranimalsplus:horseshoe_crab_blood")));
        custom.add(makeSF(new SoftFluid.Builder("tconstruct:block/sky_congealed_slime",
                "tconstruct:block/sky_congealed_slime", "sky_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:sky_slime_ball", 1)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE, SoundEvents.SLIME_BLOCK_BREAK, Items.AIR)
                .food("tconstruct:sky_slime_ball")));
        custom.add(makeSF(new SoftFluid.Builder("tconstruct:block/ichor_congealed_slime",
                "tconstruct:block/ichor_congealed_slime", "ichor_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:ichor_slime_ball", 1)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE, SoundEvents.SLIME_BLOCK_BREAK, Items.AIR)
                .food("tconstruct:ichor_slime_ball")));
        custom.add(makeSF(new SoftFluid.Builder("tconstruct:block/blood_congealed_slime",
                "tconstruct:block/blood_congealed_slime", "blood_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:blood_slime_ball", 1)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE, SoundEvents.SLIME_BLOCK_BREAK, Items.AIR)
                .food("tconstruct:blood_slime_ball")));
        custom.add(makeSF(new SoftFluid.Builder("tconstruct:block/ender_congealed_slime",
                "tconstruct:block/ender_congealed_slime", "ender_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:ender_slime_ball", 1)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE, SoundEvents.SLIME_BLOCK_BREAK, Items.AIR)
                .food("tconstruct:ender_slime_ball")));

        custom.add(makeSF(new SoftFluid.Builder("atmospheric:block/aloe_gel_block_top", Textures.POTION_TEXTURE_FLOW.toString(), "aloe_gel")
                .fromMod("atmospheric")
                .onlyFlowingTinted()
                .translationKey("item.atmospheric:aloe_gel_bottle")
                .emptyHandContainerItem("item.atmospheric:aloe_gel_block", 4)
                .drink("atmospheric:aloe_gel_bottle")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "yucca_juice")
                .fromMod("atmospheric")
                .color(0x4EE13B)
                .translationKey("item.atmospheric:yucca_juice")
                .drink("atmospheric:yucca_juice")));

        custom.add(makeSF(new SoftFluid.Builder("upgrade_aquatic:block/mulberry_jam_block_top", Textures.POTION_TEXTURE_FLOW.toString(), "mulberry_jam")
                .fromMod("upgrade_aquatic")
                .onlyFlowingTinted()
                .translationKey("item.upgrade_aquatic:mulberry_jam_bottle")
                .emptyHandContainerItem("item.upgrade_aquatic:mulberry_jam_block", 4)
                .drink("upgrade_aquatic:mulberry_jam_bottle")));


        custom.add(makeSF(new SoftFluid.Builder(Textures.DRAGON_BREATH_TEXTURE, Textures.POTION_TEXTURE_FLOW, "ambrosia")
                .fromMod("iceandfire")
                .color(0xf8a0db)
                .translationKey("item.iceandfire.ambrosia")
                .stew("iceandfire:ambrosia")));
        custom.add(makeSF(new SoftFluid.Builder("create:tea")
                .drink("create:builders_tea")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, "lavender_tea")
                .fromMod("abundance")
                .color(0x82472)
                .translationKey("item.abundance.lavender_tea")
                .drink("abundance:lavender_tea")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "scrambled_eggs")
                .fromMod("environmental")
                .color(0xdd9d23)
                .translationKey("item.environmental.scrambled_eggs")
                .stew("environmental:scrambled_eggs")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE, Textures.MILK_TEXTURE, "siren_tear")
                .fromMod("iceandfire")
                .color(0xa4e0fc)
                .translationKey("item.iceandfire.siren_tear")
                .emptyHandContainerItem("iceandfire:siren_tear", 1)));
        custom.add(makeSF(new SoftFluid.Builder(Textures.DRAGON_BREATH_TEXTURE, Textures.POTION_TEXTURE_FLOW, "mimicream")
                .fromMod("alexsmobs")
                .color(0x8071ab)
                .translationKey("item.alexsmobs.mimicream")
                .emptyHandContainerItem("alexsmobs:mimicream", 1)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE, SoundEvents.SLIME_BLOCK_BREAK, Items.AIR)));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "adzuki_milkshake")
                .fromMod("neapolitan")
                .color(0xE5828F)
                .translationKey("item.neapolitan.adzuki_milkshake")
                .bottle("adzuki_milkshake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "mint_milkshake")
                .fromMod("neapolitan")
                .color(0xA2EFB0)
                .translationKey("item.neapolitan.mint_milkshake")
                .bottle("mint_milkshake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "chocolate_milkshake")
                .fromMod("neapolitan")
                .color(0x764731)
                .translationKey("item.neapolitan.chocolate_milkshake")
                .bottle("chocolate_milkshake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "strawberry_milkshake")
                .fromMod("neapolitan")
                .color(0xEC8DAA)
                .translationKey("item.neapolitan.strawberry_milkshake")
                .bottle("strawberry_milkshake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "vanilla_milkshake")
                .fromMod("neapolitan")
                .color(0xF6CFCA)
                .translationKey("item.neapolitan.vanilla_milkshake")
                .bottle("vanilla_milkshake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "banana_milkshake")
                .fromMod("neapolitan")
                .color(0xF9D290)
                .translationKey("item.neapolitan.banana_milkshake")
                .bottle("banana_milkshake")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "strawberry_banana_smoothie")
                .fromMod("neapolitan")
                .color(0xDD66AF)
                .translationKey("item.neapolitan.strawberry_banana_smoothie")
                .bottle("strawberry_banana_smoothie")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE, Textures.POTION_TEXTURE_FLOW, "gooseberry_jam")
                .fromMod("bayou_blues")
                .color(0xF3BF40)
                .translationKey("item.bayou_blues:gooseberry_jam_bottle")
                .drink("gooseberry_jam:gooseberry_jam_bottle")));
        custom.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE, Textures.POTION_TEXTURE_FLOW, "gooseberry_juice")
                .fromMod("bayou_blues")
                .color(0xBBDF62)
                .translationKey("item.bayou_blues:gooseberry_juice_bottle")
                .drink("gooseberry_jam:gooseberry_juice_bottle")));



        custom.add(makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE, Textures.HONEY_TEXTURE, "holy_syrup")
                .fromMod("malum")
                .color(0xCC9A51)
                .translationKey("item.malum:holy_syrup")
                .drink("malum:holy_syrup")));

        custom.add(makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE, Textures.HONEY_TEXTURE, "unholy_syrup")
                .fromMod("malum")
                .color(0x902454)
                .translationKey("item.malum:holy_syrup")
                .drink("malum:unholy_syrup")));


        //inspirations dye bottles. not adding nbt mixed ones
        for (DyeColor c : DyeColor.values()) {
            Item dye = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" + c.getName() + "_dye"));
            String name = "inspirations:" + c.getSerializedName() + "_dye";
            SoftFluid s = makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE, Textures.FLOWING_WATER_TEXTURE, name)
                    .bottle(name + "d_bottle")
                    .translationKey(dye.getDescriptionId())
                    .color(ColorHelper.pack(c.getTextureDiffuseColors()))
                    .fromMod("inspirations")
                    .copyTexturesFrom("inspirations:potato_soup")
            );
            custom.add(s);
        }

        custom.forEach(SoftFluidRegistry::register);

    }

}
