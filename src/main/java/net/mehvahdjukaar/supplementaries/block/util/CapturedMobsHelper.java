package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICageJarCatchable.AnimationCategory;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

//to whoever is reading I'm sorry :/
public class CapturedMobsHelper {

    public static final List<String> COMMAND_MOBS = new ArrayList<>();

    public static final List<List<String>> DEFAULT_CONFIG = new ArrayList<>();
    public static final List<String> CATCHABLE_FISHES = new ArrayList<>();
    public static final Map<String, CapturedMobProperties> TYPES = new HashMap<>();
    //bucket item mob id,
    public static final Map<Item,String> VALID_BUCKETS = new HashMap<>();

    public static final CapturedMobProperties DEFAULT = new CapturedMobProperties("69",0,0,0,0,AnimationCategory.DEFAULT);
    public static final CapturedMobProperties MODDED_FISH = new CapturedMobProperties("420",0,0,0,1,AnimationCategory.DEFAULT);


    private static List<String> addDef(String id, int fish){
        return addDef(id,0,0.125f, 0, fish);
    }
    private static List<String> addDef(String id, float h, float w, int l, int f){
        return Arrays.asList(id,""+h,""+w,""+l,""+f);
    }
    private static List<String> addDef(String id, float h, float w, int l, AnimationCategory c){
        return Arrays.asList(id,""+h,""+w,""+l,c.toString());
    }
    private static List<String> addDef(String id, float h, float w, int l){
        return Arrays.asList(id,""+h,""+w,""+l);
    }
    private static List<String> addDef(String id, float h, float w){
        return Arrays.asList(id,""+h,""+w);
    }


    static {
        //1=default fish
        int fishIndex = 0;
        DEFAULT_CONFIG.add(addDef("minecraft:bee", 0.3125f, 0));
        DEFAULT_CONFIG.add(addDef("minecraft:vex", 0, 0.125f,0,AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("minecraft:silverfish", 0, 0.25f));
        DEFAULT_CONFIG.add(addDef("minecraft:chicken", 0.25f, 0.3125f));
        DEFAULT_CONFIG.add(addDef("minecraft:endermite",0,0,5));
        DEFAULT_CONFIG.add(addDef("supplementaries:firefly",0,0,9,AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("druidcraft:lunar_moth", 0.375f, 0.1375f,10,AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("iceandfire:pixie", 0, 0,10));
        DEFAULT_CONFIG.add(addDef("minecraft:fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("minecraft:tropical_fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("minecraft:salmon", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("minecraft:cod", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("minecraft:pufferfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("minecraft:axolotl", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:pea_wee", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:wee_wee", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:vibra_wee", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:blu_wee", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:ornate_bugfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:spindly_gem_crab", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:phantom_nudibranch", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:high_finned_blue", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:teal_arrowfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:midnight_squid", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:banded_redback_shrimp", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:flatback_sucker", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("fins:swamp_mucker", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("upgrade_aquatic:lionfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("upgrade_aquatic:nautilus", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("upgrade_aquatic:pike", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("alexsmobs:stradpole", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("alexsmobs:blobfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("unnamedanimalmod:flashlight_fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("unnamedanimalmod:elephantnose_fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("unnamedanimalmod:black_diamond_stingray", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("unnamedanimalmod:humphead_parrotfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("unnamedanimalmod:tomato_frog", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("unnamedanimalmod:pacman_frog", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("bettas:betta_fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("pogfish:pogfish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("undergarden:gwibling", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("environmental:koi", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betterendforge:end_fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:flying_fish", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:nautilus", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:eel_freshwater", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:eel_saltwater", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:lamprey", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:piranha", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("betteranimalsplus:barracuda", ++fishIndex));
        DEFAULT_CONFIG.add(addDef("rediscovered:fish", 1));
        //DEFAULT_VALUES.add(addDef("----", ++fishIndex));


        for (List<String> d : DEFAULT_CONFIG){
            if(d.size()==5){
                int f = strToInt(d.get(4));
                if(f>0 && !d.get(0).equals(""))CATCHABLE_FISHES.add(d.get(0));
            }
        }

    }


    public static String getDefaultNameFromBucket(Item bucket){
        String def = "Mob";
        String mobId = VALID_BUCKETS.getOrDefault(bucket,def);
        if(mobId.equals(def))return def;
        EntityType<?> en = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mobId));
        if(en==null)return def;
        return en.getDescription().getString();
    }

    public static CapturedMobProperties getTypeFromBucket(Item bucket){
        return getType(VALID_BUCKETS.getOrDefault(bucket,"minecraft:fish"));
    }

    public static CapturedMobProperties getType(Entity mob){
        return getType(mob.getType().getRegistryName().toString());
    }
    public static CapturedMobProperties getType(String mobId){
        return TYPES.getOrDefault(mobId, DEFAULT);
    }

    public static List<String> getFishes(){
        List<String> l = new ArrayList<>();
        for (List<String> d : DEFAULT_CONFIG){
            if(d.size()==5){
                int f = strToInt(d.get(4));
                if(f>0 && !d.get(0).equals(""))l.add(d.get(0));
            }
        }
        return l;
    }



    public static boolean isFishBucket(Item item){
        return VALID_BUCKETS.containsKey(item);
    }


    private static float strToFloat(String s){
        if(s != null && s.matches("[0-9.]+")){
            return Float.parseFloat(s);
        }
        return 0;
    }
    private static int strToInt(String s){
        if(s != null && s.matches("[0-9.]+")){
            return Integer.parseInt(s);
        }
        return 0;
    }


    public static void refresh(){
        TYPES.clear();
        List<? extends List<String>> config = ClientConfigs.block.CAPTURED_MOBS_PROPERTIES.get();
        for (List<String> l : config){
            try {
                int size = l.size();
                if (size < 2) continue;
                String id = l.get(0);
                float h = strToFloat(l.get(1));
                float w = 0;
                int light = 0;
                int fish = 0;
                AnimationCategory cat = AnimationCategory.DEFAULT;
                if (size > 2) w = strToFloat(l.get(2));
                if (size > 3) light = strToInt(l.get(3));
                if (size > 4) {
                    String type = l.get(4).toLowerCase();
                    switch (type) {
                        case "air":
                            cat = AnimationCategory.AIR;
                            break;
                        case "land":
                            cat = AnimationCategory.LAND;
                            break;
                        case "floating":
                            cat = AnimationCategory.FLOATING;
                            break;
                        default:
                            fish = strToInt(type);
                            if (fish > 0) cat = AnimationCategory.FISH;
                            break;
                    }
                }
            /*
            if(size>5) {
                addValidBucket(l.get(5));
            }else{
                tryAddingValidBucket(id);
            }*/
                CapturedMobProperties type = new CapturedMobProperties(id, h, w, light, fish, cat);
                TYPES.put(id, type);
            }
            catch (Exception e){
                Supplementaries.LOGGER.warn("failed to load captured mob configs");
            }
        }

        //TODO: redo this

        for (String id : CATCHABLE_FISHES){
            tryAddingValidBucket(id);
        }
    }

    public static void tryAddingValidBucket(String mobId){
        ResourceLocation res = new ResourceLocation(mobId);
        Item bucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation(res.getNamespace(),res.getPath()+"_bucket"));
        if(bucket!=null && bucket!=Items.AIR){
            VALID_BUCKETS.put(bucket, mobId);
        }
        else{
            bucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation(res.getNamespace(),"bucket_"+res.getPath()));
            if(bucket!=null && bucket!=Items.AIR){
                VALID_BUCKETS.put(bucket, mobId);
            }
            else{
                bucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation(res.getNamespace(),"bucket_of_"+res.getPath()));
                if(bucket!=null && bucket!=Items.AIR){
                    VALID_BUCKETS.put(bucket, mobId);
                }
            }
        }
    }


    public static class CapturedMobProperties {
        private final String id;
        private final float extraWidth;
        private final float extraHeight;
        private final int lightLevel;
        private final int fishTexture;
        private final AnimationCategory category;

        private CapturedMobProperties(String id, float h, float w, int light, int fish, AnimationCategory c){
            this.id = id;
            this.extraWidth = w;
            this.extraHeight = h;
            this.lightLevel = light;
            this.fishTexture = fish-1;
            this.category = c;
        }

        public AnimationCategory getCategory() {
            return category;
        }

        public boolean canHaveWater() {
            return this.isFlying()||this.isFish();
        }
        public String getId() {
            return id;
        }
        public float getHeight() {
            return extraHeight;
        }
        public float getWidth() {
            return extraWidth;
        }
        public int getFishTexture() {
            return fishTexture;
        }
        public int getLightLevel() {
            return lightLevel;
        }
        public boolean isFish() {
            return this.category.isFish();
        }
        public boolean isFlying() {
            return this.category.isFlying();
        }
        public boolean isLand() {
            return this.category.isLand();
        }
        public boolean isFloating(){
            return this.category.isFloating();
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }


}
