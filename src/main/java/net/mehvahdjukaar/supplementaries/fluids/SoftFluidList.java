package net.mehvahdjukaar.supplementaries.fluids;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;

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
    public static SoftFluid EMPTY;
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

    private static Supplier<ItemStack> waterBottleSupplier(){
        return ()->PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);
    }

    public static SoftFluid makeSF(SoftFluid.Builder builder){
        if(builder.isDisabled)return null;
        return new SoftFluid(builder);
    }


    public static void init() {
        EMPTY = makeSF(new SoftFluid.Builder(Fluids.EMPTY));
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
                .translationKey("fluid.supplementaries.honey")
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
        List<SoftFluid> customFluidsList = new ArrayList<>(Arrays.asList(WATER,LAVA,HONEY,MILK,MUSHROOM_STEW,
                SUS_STEW,BEETROOT_SOUP,RABBIT_STEW,POTION,DRAGON_BREATH,XP));

        //fix null return here
        customFluidsList.add(makeSF(new SoftFluid.Builder(Textures.POTION_TEXTURE,Textures.POTION_TEXTURE_FLOW,"komodo_spit")
                .color(0xa8b966)
                .bottle("alexsmobs:komodo_spit_bottle")));
        customFluidsList.add(makeSF(new SoftFluid.Builder(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW,"squash_soup")
                .color(0xe6930a)
                .bowl("simplefarming:squash_soup")
                .food("simplefarming:squash_soup")));
        customFluidsList.add(makeSF(new SoftFluid.Builder("inspirations:potato_soup")
                .textures(Textures.SOUP_TEXTURE,Textures.POTION_TEXTURE_FLOW)
                .bowl("inspirations:potato_soup")
                .food("inspirations:potato_soup")));

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
            customFluidsList.add(s);
        }

        for(SoftFluid s : customFluidsList){
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
            if(f.getRegistryName().getNamespace().equals("create")){
                int a =1;
            }
            boolean eq = false;
            for (SoftFluid s : customFluidsList){
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
