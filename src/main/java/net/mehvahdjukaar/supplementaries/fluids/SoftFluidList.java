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
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class SoftFluidList {
    // id -> SoftFluid
    public static final HashMap<String,SoftFluid> ID_MAP = new HashMap<>();
    // filled item -> SoftFluid. need to handle potions separately since they map to same item id
    private static final HashMap<Item,SoftFluid> ITEM_MAP = new HashMap<>();
    // fluid item -> SoftFluid
    private static final HashMap<Fluid,SoftFluid> FLUID_MAP = new HashMap<>();

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
                .sound(SoundEvents.ITEM_BUCKET_FILL_LAVA,SoundEvents.ITEM_BUCKET_EMPTY_LAVA));
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
                .bottle("vanillacookbook:milk_bottle")
                .bottle("simplefarming:milk_bottle")
                .bottle("farmersdelight:milk_bottle"));
        MUSHROOM_STEW = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"mushroom_stew")
                .color(0xffad89)
                .translationKey(Items.MUSHROOM_STEW.getTranslationKey())
                .bowl(Items.MUSHROOM_STEW)
                .addEqFluid("inspirations:mushroom_stew")
                .textureOverrideF("inspirations:mushroom_stew")
        );
        BEETROOT_SOUP = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"beetroot_soup")
                .color(0xC93434)
                .translationKey(Items.BEETROOT_SOUP.getTranslationKey())
                .bowl(Items.BEETROOT_SOUP)
                .addEqFluid("inspirations:beetroot_soup")
                .textureOverrideF("inspirations:beetroot_soup")
        );
        RABBIT_STEW = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"rabbit_stew")
                .color(0xFF904F)
                .translationKey(Items.RABBIT_STEW.getTranslationKey())
                .bowl(Items.RABBIT_STEW)
                .addEqFluid("inspirations:rabbit_stew")
                .textureOverrideF("inspirations:rabbit_stew")
        );
        SUS_STEW = makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"suspicious_stew")
                .color(0xBAE85F)
                .translationKey(Items.SUSPICIOUS_STEW.getTranslationKey())
                .bowl(Items.SUSPICIOUS_STEW)
                .textureOverrideF("inspirations:mushroom_stew")
        );
        POTION = makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"potion")
                .color(PotionUtils.getPotionColor(Potions.EMPTY))
                .translationKey(Items.POTION.getTranslationKey())
                .bottle(Items.POTION)
                .food(Items.POTION)
                .addEqFluid("create:potion"));
        DRAGON_BREATH = makeSF(new SoftFluid.Builder(Textures.DRAGON_BREATH_TEXTURE,Textures.POTION_TEXTURE_FLOW,"dragon_breath")
                .color(0xFF33FF)
                .luminosity(3)
                .translationKey(Items.DRAGON_BREATH.getTranslationKey())
                .bottle(Items.DRAGON_BREATH));
        XP = makeSF(new SoftFluid.Builder(Textures.XP_TEXTURE,Textures.XP_TEXTURE_FLOW,"experience")
                .translationKey("fluid.supplementaries.experience")
                .bottle(Items.EXPERIENCE_BOTTLE));
        SLIME = makeSF(new SoftFluid.Builder(Textures.SLIME_TEXTURE,Textures.SLIME_TEXTURE,"slime")
                .bottle(Items.SLIME_BALL)
                //TODO: fix to accoumt for left hand
                .specialEmptyBottle(Items.WHEAT_SEEDS)
                .translationKey("fluid.supplementaries.slime"));
        List<SoftFluid> custom = new ArrayList<>(Arrays.asList(WATER,LAVA,HONEY,MILK,MUSHROOM_STEW,
                SUS_STEW,BEETROOT_SOUP,RABBIT_STEW,POTION,DRAGON_BREATH,XP,SLIME));


        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"komodo_spit")
                .condition("alexsmobs")
                .color(0xa8b966)
                .bottle("alexsmobs:komodo_spit_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"squash_soup")
                .condition("simplefarming")
                .color(0xe6930a)
                .translationKey("simplefarming:squash_soup")
                .bowl("simplefarming:squash_soup")
                .food("simplefarming:squash_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder("inspirations:potato_soup")
                .textures(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW)
                .condition("inspirations")
                .translationKey("inspirations:potato_soup")
                .bowl("inspirations:potato_soup")
                .food("inspirations:potato_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,"fish_oil")
                .condition("alexsmobs")
                .color(0xFFE89C)
                .translationKey("alexsmobs:fish_oil")
                .food("alexsmobs:fish_oil")
                .bottle("alexsmobs:fish_oil")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"poison")
                .condition("alexsmobs")
                .color(0x8AEB67)
                .translationKey("alexsmobs:poison_bottle")
                .bottle("alexsmobs:poison_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"sopa_de_macaco")
                .condition("alexsmobs")
                .color(0xB6C184)
                .translationKey("alexsmobs:sopa_de_macaco")
                .food("alexsmobs:sopa_de_macaco")
                .bowl("alexsmobs:sopa_de_macaco")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"baked_cod_stew")
                .condition("farmersdelight")
                .color(0xECCD96)
                .food("farmersdelight:baked_cod_stew")
                .translationKey("farmersdelight:baked_cod_stew")
                .bowl("farmersdelight:baked_cod_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"beef_stew")
                .condition("farmersdelight")
                .color(0x713F2D)
                .food("farmersdelight:beef_stew")
                .translationKey("farmersdelight:beef_stew")
                .bowl("farmersdelight:beef_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"chicken_soup")
                .condition("farmersdelight")
                .color(0xDEA766)
                .food("farmersdelight:chicken_soup")
                .translationKey("farmersdelight:chicken_soup")
                .bowl("farmersdelight:chicken_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"fish_stew")
                .condition("farmersdelight")
                .color(0xB34420)
                .food("farmersdelight:fish_stew")
                .translationKey("farmersdelight:fish_stew")
                .bowl("farmersdelight:fish_stew")));
        //TODO: add honey and milk flowing textures
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.MILK_TEXTURE,Textures.MILK_TEXTURE,"hot_cocoa")
                .condition("farmersdelight")
                //.textureOverride("create:chocolate",0xe98352)
                .color(0x8F563B)
                .food("farmersdelight:hot_cocoa")
                .translationKey("farmersdelight:hot_cocoa")
                .bottle("farmersdelight:hot_cocoa")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"pumpkin_soup")
                .condition("farmersdelight")
                .color(0xE38A1D)
                .food("farmersdelight:pumpkin_soup")
                .translationKey("farmersdelight:pumpkin_soup")
                .bowl("farmersdelight:pumpkin_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"tomato_sauce")
                .condition("farmersdelight")
                .color(0xC0341F)
                .food("farmersdelight:tomato_sauce")
                .translationKey("farmersdelight:tomato_sauce")
                .bowl("farmersdelight:tomato_sauce")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.HONEY_TEXTURE,Textures.POTION_TEXTURE_FLOW,"syrup")
                .condition("autumnity")
                .textureOverride("create:honey")
                .color(0x893217)
                .food("autumnity:syrup_bottle")
                .translationKey("autumnity:syrup_bottle")
                .bottle("autumnity:syrup_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"fire_stew")
                .condition("iceandfire")
                .color(0xEB5D10)
                .food("iceandfire:fire_stew")
                .translationKey("iceandfire:fire_stew")
                .bowl("iceandfire:fire_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"frost_stew")
                .condition("iceandfire")
                .color(0x81F2F9)
                .food("iceandfire:frost_stew")
                .translationKey("iceandfire:frost_stew")
                .bowl("iceandfire:frost_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"lightning_stew")
                .condition("iceandfire")
                .color(0x7552C2)
                .food("iceandfire:lightning_stew")
                .translationKey("iceandfire:lightning_stew")
                .bowl("iceandfire:lightning_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"fire_dragon_blood")
                .condition("iceandfire")
                .color(0x1BCFFC)
                .food("iceandfire:fire_dragon_blood")
                .translationKey("iceandfire:fire_dragon_blood")
                .bottle("iceandfire:fire_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"lightning_dragon_blood")
                .condition("iceandfire")
                .color(0xA700FC)
                .food("iceandfire:lightning_dragon_blood")
                .translationKey("iceandfire:lightning_dragon_blood")
                .bottle("iceandfire:lightning_dragon_blood")));

        //inspirations dye bottles. not adding nbt mixed ones
        for (DyeColor c: DyeColor.values()){
            String name = "inspirations:"+c.getString()+"_dyed_bottle";
            SoftFluid s = makeSF(new SoftFluid.Builder(Textures.WATER_TEXTURE,Textures.FLOWING_WATER_TEXTURE,name)
                    .bottle(name)
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
            if(f instanceof FlowingFluid && ((FlowingFluid) f).getStillFluid()!=f)continue;
            if(f instanceof ForgeFlowingFluid.Flowing || f==Fluids.EMPTY)continue;
            boolean eq = false;
            for (SoftFluid s : custom){
                if(s.isEquivalent(f)){
                    //is equivalent, map fluid and item to it
                    tryAddItem(f.getFilledBucket(),s);
                    FLUID_MAP.put(f,s);
                    eq=true;
                    break;
                }
            }
            if(eq)continue;
            //is not equivalent: create new SoftFluid
            SoftFluid newSF = new SoftFluid(new SoftFluid.Builder(f));
            tryAddItem(f.getFilledBucket(),newSF);
            FLUID_MAP.put(f,newSF);
            forgeFluidsList.add(newSF);
        }
        for(SoftFluid s : forgeFluidsList){
            ID_MAP.put(s.getID(),s);
        }

        int a= 1;

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
