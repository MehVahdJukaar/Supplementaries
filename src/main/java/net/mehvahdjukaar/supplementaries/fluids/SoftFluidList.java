package net.mehvahdjukaar.supplementaries.fluids;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class SoftFluidList {
    // id -> SoftFluid
    public static final HashMap<String,SoftFluid> ID_MAP = new HashMap<>();
    // filled item -> SoftFluid. need to handle potions separately since they map to same item id
    public static final HashMap<Item,SoftFluid> ITEM_MAP = new HashMap<>();
    // fluid item -> SoftFluid
    public static final HashMap<Fluid,SoftFluid> FLUID_MAP = new HashMap<>();

    public static SoftFluid fromID(String id){
        return ID_MAP.getOrDefault(id, EMPTY);
    }

    //TODO: make final
    public static SoftFluid EMPTY = makeSF(new SoftFluid.Builder(Fluids.EMPTY));
    public static SoftFluid WATER;
    public static SoftFluid LAVA;
    public static SoftFluid HONEY;
    public static SoftFluid MILK;
    public static SoftFluid MUSHROOM_STEW;
    public static SoftFluid BEETROOT_SOUP;
    public static SoftFluid RABBIT_STEW;
    public static SoftFluid SUS_STEW;
    public static SoftFluid POTION;
    public static SoftFluid DRAGON_BREATH;
    public static SoftFluid XP;
    public static SoftFluid SLIME;
    public static SoftFluid DIRT;
    public static SoftFluid GHAST_TEAR;
    public static SoftFluid MAGMA_CREAM;
    public static SoftFluid SAP;


    public static SoftFluid makeSF(SoftFluid.Builder builder){
        if(builder.isDisabled)return null;
        return new SoftFluid(builder);
    }

    public static void addOpt(List<SoftFluid> l, SoftFluid s){
        if(s!=null)l.add(s);
    }


    public static void init() {
        WATER = makeSF(new SoftFluid.Builder(Fluids.WATER)
                //dont put bottles here
                .food(Items.POTION)); //handled via special case in liquid holder along other nbt stff
        LAVA = makeSF(new SoftFluid.Builder(Fluids.LAVA)
                .bottle("alexsmobs:lava_bottle")
                .bucket(Items.LAVA_BUCKET)
                .sound(SoundEvents.BUCKET_FILL_LAVA,SoundEvents.BUCKET_EMPTY_LAVA));
        HONEY = makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE,Textures.HONEY_TEXTURE,"honey")
                .translationKey("fluid.supplementaries.honey")
                .bottle(Items.HONEY_BOTTLE)
                .textureOverrideF("create:honey")
                .addEqFluid("create:honey")
                .addEqFluid("inspirations:honey"));
        MILK = makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE,Textures.POTION_TEXTURE_FLOW,"milk")
                .bucket(Items.MILK_BUCKET)
                .food(Items.MILK_BUCKET)
                .translationKey("fluid.supplementaries.milk")
                .textureOverrideF("create:milk")
                .addEqFluid("create:milk")
                .addEqFluid("inspirations:milk")
                .bottle("farmersdelight:milk_bottle")
                .bottle("neapolitan:milk_bottle")
                .bottle("fluffy_farmer:bottle_of_milk")
                .bottle("vanillacookbook:milk_bottle")
                .bottle("simplefarming:milk_bottle")
                .bottle("farmersdelight:milk_bottle"));
        MUSHROOM_STEW = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"mushroom_stew")
                .color(0xffad89)
                .bowl(Items.MUSHROOM_STEW)
                .addEqFluid("inspirations:mushroom_stew")
                .textureOverrideF("inspirations:mushroom_stew")
        );
        BEETROOT_SOUP = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"beetroot_soup")
                .color(0xC93434)
                .bowl(Items.BEETROOT_SOUP)
                .addEqFluid("inspirations:beetroot_soup")
                .textureOverrideF("inspirations:beetroot_soup")
        );
        RABBIT_STEW = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"rabbit_stew")
                .color(0xFF904F)
                .bowl(Items.RABBIT_STEW)
                .addEqFluid("inspirations:rabbit_stew")
                .textureOverrideF("inspirations:rabbit_stew")
        );
        SUS_STEW = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"suspicious_stew")
                .color(0xBAE85F)
                .bowl(Items.SUSPICIOUS_STEW)
                .textureOverrideF("inspirations:mushroom_stew")
        );
        //TODO: automate translation key thing
        POTION = makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"potion")
                .color(PotionUtils.getColor(Potions.EMPTY))
                .translationKey(Items.POTION.getDescriptionId())
                .bottle(Items.POTION)
                .food(Items.POTION)
                .addEqFluid("create:potion"));
        DRAGON_BREATH = makeSF(new SoftFluid.Builder(Textures.DRAGON_BREATH_TEXTURE,Textures.POTION_TEXTURE_FLOW,"dragon_breath")
                .color(0xFF33FF)
                .luminosity(3)
                .translationKey(Items.DRAGON_BREATH.getDescriptionId())
                .bottle(Items.DRAGON_BREATH));
        XP = makeSF(new SoftFluid.Builder(Textures.XP_TEXTURE,Textures.XP_TEXTURE_FLOW,"experience")
                .translationKey("fluid.supplementaries.experience")
                .bottle(Items.EXPERIENCE_BOTTLE));
        SLIME = makeSF(new SoftFluid.Builder(Textures.SLIME_TEXTURE,Textures.SLIME_TEXTURE,"slime")
                .bottle(Items.SLIME_BALL)
                .specialEmptyBottle(Items.AIR)
                .emptySound(SoundEvents.SLIME_BLOCK_PLACE)
                .translationKey("fluid.supplementaries.slime"));
        DIRT = makeSF(new SoftFluid.Builder(Textures.DIRT_TEXTURE,Textures.DIRT_TEXTURE,"dirt")
                .bottle(Items.DIRT)
                .specialEmptyBottle(Items.AIR)
                .emptySound(SoundEvents.GRAVEL_PLACE)
                .translationKey("block.minecraft.dirt"));
        GHAST_TEAR = makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE,Textures.POTION_TEXTURE_FLOW,"ghast_tear")
                .bottle(Items.GHAST_TEAR)
                .color(0xbff0f0)
                .specialEmptyBottle(Items.AIR)
                .translationKey("item.minecraft.ghast_tear"));
        MAGMA_CREAM = makeSF(new SoftFluid.Builder(Textures.MAGMA_TEXTURE,Textures.MAGMA_TEXTURE_FLOW,"magma_cream")
                .bottle(Items.MAGMA_CREAM)
                .specialEmptyBottle(Items.AIR)
                .translationKey("item.minecraft.magma_cream"));
        SAP = makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE,Textures.POTION_TEXTURE_FLOW,"sap")
                .textureOverride("thermal:sap")
                .color(0xbd6e2a)
                .drink("autumnity:sap_bottle")
                .bucket("thermal:sap_bucket")
                .translationKey("fluid.supplementaries.sap"));
        List<SoftFluid> custom = new ArrayList<>(Arrays.asList(WATER,LAVA,HONEY,MILK,MUSHROOM_STEW,
                SUS_STEW,BEETROOT_SOUP,RABBIT_STEW,POTION,DRAGON_BREATH,XP,SLIME,DIRT,GHAST_TEAR,SAP));


        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"komodo_spit")
                .condition("alexsmobs")
                .color(0xa8b966)
                .translationKey("item.alexmobs.komodo_spit")
                .bottle("alexsmobs:komodo_spit_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"squash_soup")
                .condition("simplefarming")
                .color(0xe6930a)
                .bowl("simplefarming:squash_soup")
                .food("simplefarming:squash_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder("inspirations:potato_soup")
                .textures(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW)
                .condition("inspirations")
                .bowl("inspirations:potato_soup")
                .food("inspirations:potato_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"fish_oil")
                .condition("alexsmobs")
                .color(0xFFE89C)
                .translationKey("item.alexsmobs.fish_oil")
                .food("alexsmobs:fish_oil")
                .bottle("alexsmobs:fish_oil")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"poison")
                .condition("alexsmobs")
                .color(0x8AEB67)
                .translationKey("item.alexsmobs:poison")
                .bottle("alexsmobs:poison_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"sopa_de_macaco")
                .condition("alexsmobs")
                .color(0xB6C184)
                .food("alexsmobs:sopa_de_macaco")
                .bowl("alexsmobs:sopa_de_macaco")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"baked_cod_stew")
                .condition("farmersdelight")
                .color(0xECCD96)
                .food("farmersdelight:baked_cod_stew")
                .bowl("farmersdelight:baked_cod_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"beef_stew")
                .condition("farmersdelight")
                .color(0x713F2D)
                .food("farmersdelight:beef_stew")
                .bowl("farmersdelight:beef_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"chicken_soup")
                .condition("farmersdelight")
                .color(0xDEA766)
                .food("farmersdelight:chicken_soup")
                .bowl("farmersdelight:chicken_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"fish_stew")
                .condition("farmersdelight")
                .color(0xB34420)
                .food("farmersdelight:fish_stew")
                .bowl("farmersdelight:fish_stew")));
        //TODO: add honey and milk flowing textures
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE,Textures.MILK_TEXTURE,"hot_cocoa")
                .condition("farmersdelight")
                //.textureOverride("create:chocolate",0xe98352)
                .color(0x8F563B)
                .food("farmersdelight:hot_cocoa")
                .translationKey("item.farmersdelight.hot_cocoa")
                .bottle("farmersdelight:hot_cocoa")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"pumpkin_soup")
                .condition("farmersdelight")
                .color(0xE38A1D)
                .food("farmersdelight:pumpkin_soup")
                .bowl("farmersdelight:pumpkin_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"tomato_sauce")
                .condition("farmersdelight")
                .color(0xC0341F)
                .food("farmersdelight:tomato_sauce")
                .bowl("farmersdelight:tomato_sauce")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE,Textures.POTION_TEXTURE_FLOW,"syrup")
                .condition("autumnity")
                .textureOverride("create:honey")
                .color(0x8e3f26)
                .addEqFluid("thermal:syrup")
                .food("autumnity:syrup_bottle")
                .translationKey("item.autumnity.syrup")
                .bottle("autumnity:syrup_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"fire_stew")
                .condition("iceandfire")
                .color(0xEB5D10)
                .food("iceandfire:fire_stew")
                .bowl("iceandfire:fire_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"frost_stew")
                .condition("iceandfire")
                .color(0x81F2F9)
                .food("iceandfire:frost_stew")
                .bowl("iceandfire:frost_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"lightning_stew")
                .condition("iceandfire")
                .color(0x7552C2)
                .food("iceandfire:lightning_stew")
                .bowl("iceandfire:lightning_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"fire_dragon_blood")
                .condition("iceandfire")
                .color(0xEB5D10)
                .food("iceandfire:fire_dragon_blood")
                .translationKey("item.iceandfire.fire_dragon_blood")
                .bottle("iceandfire:fire_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"lightning_dragon_blood")
                .condition("iceandfire")
                .color(0xA700FC)
                .food("iceandfire:lightning_dragon_blood")
                .translationKey("item.iceandfire.lightning_dragon_blood")
                .bottle("iceandfire:lightning_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"ice_dragon_blood")
                .condition("iceandfire")
                .color(0x1BCFFC)
                .food("iceandfire:ice_dragon_blood")
                .translationKey("item.iceandfire.ice_dragon_blood")
                .bottle("iceandfire:ice_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"vegetable_soup")
                .condition("farmersdelight")
                .color(0x8A7825)
                .food("farmersdelight:vegetable_soup")
                .bowl("farmersdelight:vegetable_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE,Textures.MILK_TEXTURE,"goat_milk")
                .condition("betteranimalsplus")
                .translationKey("item.betteranimalsplus.goatmilk")
                .bucket("betteranimalsplus:goatmilk")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"whiskey")
                .condition("simplefarming")
                .color(0xd29062)
                .translationKey("item.simplefarming.whiskey")
                .food("simplefarming:whiskey")
                .bottle("simplefarming:whiskey")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"olive_oil")
                .condition("simplefarming")
                .color(0x969F1C)
                .translationKey("item.simplefarming.olive_oil")
                .food("simplefarming:olive_oil")
                .bottle("simplefarming:olive_oil")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"vinegar")
                .condition("simplefarming")
                .color(0xD4D2C4)
                .translationKey("item.simplefarming.vinegar")
                .food("simplefarming:vinegar")
                .bottle("simplefarming:vinegar")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"mead")
                .condition("simplefarming")
                .color(0xC39710)
                .translationKey("item.simplefarming.mead")
                .food("simplefarming:mead")
                .bottle("simplefarming:mead")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"beer")
                .condition("simplefarming")
                .color(0xCB9847)
                .translationKey("item.simplefarming.beer")
                .food("simplefarming:beer")
                .bottle("simplefarming:beer")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"cauim")
                .condition("simplefarming")
                .color(0xCFC273)
                .translationKey("item.simplefarming.cauim")
                .food("simplefarming:cauim")
                .bottle("simplefarming:cauim")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"cider")
                .condition("simplefarming")
                .color(0xDC921E)
                .translationKey("item.simplefarming.cider")
                .food("simplefarming:cider")
                .bottle("simplefarming:cider")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"sake")
                .condition("simplefarming")
                .color(0xE3D56C)
                .translationKey("item.simplefarming.sake")
                .drink("simplefarming:sake")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"tiswin")
                .condition("simplefarming")
                .color(0xDC5826)
                .translationKey("item.simplefarming.tiswin")
                .drink("simplefarming:tiswin")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"vodka")
                .condition("simplefarming")
                .color(0xCFDFEB)
                .translationKey("item.simplefarming.vodka")
                .drink("simplefarming:vodka")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"wine")
                .condition("simplefarming")
                .color(0x961D49)
                .translationKey("item.simplefarming.wine")
                .drink("simplefarming:wine")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE,Textures.HONEY_TEXTURE,"jam")
                .condition("simplefarming")
                .color(0x970C1F)
                .translationKey("item.simplefarming.jam")
                .food("simplefarming:jam")
                .bottle("simplefarming:jam")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"umbrella_cluster_juice")
                .condition("betterendforge")
                .color(0xBE53F6)
                .translationKey("item.betterendforge.umbrella_cluster_juice")
                .drink("betterendforge:umbrella_cluster_juice")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"soap")
                .condition("fluffy_farmer")
                .color(0xdb9eff)
                .translationKey("item.fluffy_farmer.soap")
                .bottle("fluffy_farmer:bottle_with_soap_bubbles")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"soap")
                .condition("betteranimalsplus")
                .color(0x60A8E0)
                .translationKey("item.betteranimalsplus.horseshoe_crab_blood")
                .drink("betteranimalsplus:horseshoe_crab_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(new ResourceLocation("tconstruct:sky_congealed_slime"),
                new ResourceLocation("tconstruct:sky_congealed_slime"),"sky_slime")
                .condition("tconstruct")
                .specialItem("tconstruct:sky_slime_ball")
                .food("tconstruct:sky_slime_ball")));
        addOpt(custom,makeSF(new SoftFluid.Builder(new ResourceLocation("tconstruct:ichor_congealed_slime"),
                new ResourceLocation("tconstruct:ichor_congealed_slime"),"ichor_slime")
                .condition("tconstruct")
                .specialItem("tconstruct:ichor_slime_ball")
                .food("tconstruct:ichor_slime_ball")));
        addOpt(custom,makeSF(new SoftFluid.Builder(new ResourceLocation("tconstruct:blood_congealed_slime"),
                new ResourceLocation("tconstruct:blood_congealed_slime"),"blood_slime")
                .condition("tconstruct")
                .specialItem("tconstruct:blood_slime_ball")
                .food("tconstruct:blood_slime_ball")));
        addOpt(custom,makeSF(new SoftFluid.Builder(new ResourceLocation("tconstruct:ender_congealed_slime"),
                new ResourceLocation("tconstruct:ender_congealed_slime"),"ender_slime")
                .condition("tconstruct")
                .specialItem("tconstruct:ender_slime_ball")
                .food("tconstruct:ender_slime_ball")));

        //inspirations dye bottles. not adding nbt mixed ones
        for (DyeColor c: DyeColor.values()){
            Item dye = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:"+c.getName()+"_dye"));
            String name = "inspirations:"+c.getSerializedName()+"_dyed_bottle";
            SoftFluid s = makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,name)
                    .bottle(name)
                    .translationKey(dye.getDescriptionId())
                    .color(c.getColorValue())
                    .condition("inspirations")
                    .textureOverride("inspirations:potato_soup")
            );
            if(s==null)continue;
            custom.add(s);
        }

        for(SoftFluid s : custom){
            if(s==null)continue;
            ID_MAP.put(s.getID(),s);
            if(s.hasBowl())tryAddItems(s.getBowls(),s);
            if(s.hasBottle())tryAddItems(s.getBottles(),s);
            if(s.hasBucket())tryAddItems(s.getBuckets(),s);
        }

        //THIS IS HORRIBLE
        List<SoftFluid> forgeFluidsList = new ArrayList<>();
        for (Fluid f : ForgeRegistries.FLUIDS){
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid && ((FlowingFluid) f).getSource() != f) continue;
                if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                boolean eq = false;
                for (SoftFluid s : custom) {
                    if (s.isEquivalent(f)) {
                        //is equivalent, map fluid and item to it
                        tryAddItem(f.getBucket(), s);
                        FLUID_MAP.put(f, s);
                        eq = true;
                        break;
                    }
                }
                if (eq) continue;
                //is not equivalent: create new SoftFluid
                SoftFluid newSF = new SoftFluid(new SoftFluid.Builder(f));
                tryAddItem(f.getBucket(), newSF);
                FLUID_MAP.put(f, newSF);
                forgeFluidsList.add(newSF);
            }
            catch (Exception ignored){}
        }
        for(SoftFluid s : forgeFluidsList){
            ID_MAP.put(s.getID(),s);
        }


    }

    private static void tryAddItems(Collection<Item> c, SoftFluid s){
        for (Item i : c){
            tryAddItem(i,s);
        }
    }

    private static void tryAddItem(Item i,SoftFluid s){
        if(!ITEM_MAP.containsKey(i)){
            ITEM_MAP.put(i,s);
        }
    }

    public static SoftFluid fromFluid(Fluid fluid){
        return FLUID_MAP.getOrDefault(fluid,EMPTY);
    }

    public static SoftFluid fromItem(Item item){
        return ITEM_MAP.getOrDefault(item,EMPTY);
    }

}
