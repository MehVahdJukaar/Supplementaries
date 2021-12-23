package net.mehvahdjukaar.supplementaries.common.capabilities.mobholder;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.ConfigHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraftforge.fml.ModList;

import java.util.*;

//to whoever is reading I'm sorry :/
public class CapturedMobsHelper {

    public static final List<String> COMMAND_MOBS = new ArrayList<>();
    public static final List<List<String>> DEFAULT_CONFIG = new ArrayList<>();

    private static final List<String> FISHES_2D = new ArrayList<>();
    private static final Map<String, CapturedMobConfigProperties> TYPES = new HashMap<>();


    //random ids that don't map to any actual mobs
    private static final CapturedMobConfigProperties DEFAULT = new CapturedMobConfigProperties("69", 0, 0, 0, 0, AnimationCategory.DEFAULT);
    private static final CapturedMobConfigProperties MODDED_FISH = new CapturedMobConfigProperties("420", 0, 0, 0, 1, AnimationCategory.DEFAULT);


    public static boolean is2DFish(EntityType<?> type){
        return FISHES_2D.contains(type.getRegistryName().toString());
    }

    private static List<String> addFish(String id) {
        return addDef(id, 0, 0.125f, 0, ++fishIndex);
    }

    private static List<String> addFish(String id, int fishIndex) {
        return addDef(id, 0, 0.125f, 0, fishIndex);
    }

    private static List<String> addDef(String id, float h, float w, int l, int f) {
        return Arrays.asList(id, "" + h, "" + w, "" + l, "" + f);
    }

    private static List<String> addDef(String id, float h, float w, int l, AnimationCategory c) {
        return Arrays.asList(id, "" + h, "" + w, "" + l, c.toString());
    }

    private static List<String> addDef(String id, float h, float w, int l) {
        return Arrays.asList(id, "" + h, "" + w, "" + l);
    }

    private static List<String> addDef(String id, float h, float w) {
        return Arrays.asList(id, "" + h, "" + w);
    }

    private static int fishIndex = 0;

    static {
        //1=default fish

        DEFAULT_CONFIG.add(addDef("minecraft:bee", 0.3125f, 0f));
        DEFAULT_CONFIG.add(addDef("minecraft:vex", 0, 0.125f, 0, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("minecraft:silverfish", 0, 0.25f));
        DEFAULT_CONFIG.add(addDef("minecraft:chicken", 0.25f, 0.3125f));
        DEFAULT_CONFIG.add(addDef("minecraft:endermite", 0, 0, 5));
        DEFAULT_CONFIG.add(addDef("minecraft:fox", 0, 0.2f));
        DEFAULT_CONFIG.add(addDef("minecraft:squid", 0.25f, 0.25f, 0, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("minecraft:glow_squid", 0.25f, 0.25f, 3, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("supplementaries:firefly", 0, 0, 9, AnimationCategory.FLOATING));

        DEFAULT_CONFIG.add(addDef("druidcraft:lunar_moth", 0.375f, 0.1375f, 10, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("iceandfire:pixie", 0, 0, 10));

        DEFAULT_CONFIG.add(addDef("feywild:winter_pixie", 0.125f, 0f, 8, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("feywild:summer_pixie", 0.125f, 0f, 8, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("feywild:spring_pixie", 0.125f, 0f, 8, AnimationCategory.FLOATING));
        DEFAULT_CONFIG.add(addDef("feywild:autumn_pixie", 0.25f, 0f, 8, AnimationCategory.FLOATING));


        DEFAULT_CONFIG.add(addFish("minecraft:fish"));
        DEFAULT_CONFIG.add(addFish("minecraft:tropical_fish"));
        DEFAULT_CONFIG.add(addFish("minecraft:salmon"));
        DEFAULT_CONFIG.add(addFish("minecraft:cod"));
        DEFAULT_CONFIG.add(addFish("minecraft:pufferfish"));
        DEFAULT_CONFIG.add(addFish("minecraft:axolotl"));
        DEFAULT_CONFIG.add(addFish("fins:pea_wee"));
        DEFAULT_CONFIG.add(addFish("fins:wee_wee"));
        DEFAULT_CONFIG.add(addFish("fins:vibra_wee"));
        DEFAULT_CONFIG.add(addFish("fins:blu_wee"));
        DEFAULT_CONFIG.add(addFish("fins:ornate_bugfish"));
        DEFAULT_CONFIG.add(addFish("fins:spindly_gem_crab"));
        DEFAULT_CONFIG.add(addFish("fins:phantom_nudibranch"));
        DEFAULT_CONFIG.add(addFish("fins:high_finned_blue"));
        DEFAULT_CONFIG.add(addFish("fins:teal_arrowfish"));
        DEFAULT_CONFIG.add(addFish("fins:midnight_squid"));
        DEFAULT_CONFIG.add(addFish("fins:banded_redback_shrimp"));
        DEFAULT_CONFIG.add(addFish("fins:flatback_sucker"));
        DEFAULT_CONFIG.add(addFish("fins:swamp_mucker"));
        DEFAULT_CONFIG.add(addFish("upgrade_aquatic:lionfish"));
        DEFAULT_CONFIG.add(addFish("upgrade_aquatic:nautilus"));
        DEFAULT_CONFIG.add(addFish("upgrade_aquatic:pike"));
        DEFAULT_CONFIG.add(addFish("alexsmobs:stradpole"));
        DEFAULT_CONFIG.add(addFish("alexsmobs:blobfish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:flashlight_fish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:elephantnose_fish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:black_diamond_stingray"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:humphead_parrotfish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:tomato_frog"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:pacman_frog"));
        DEFAULT_CONFIG.add(addFish("bettas:betta_fish"));
        DEFAULT_CONFIG.add(addFish("pogfish:pogfish"));
        DEFAULT_CONFIG.add(addFish("undergarden:gwibling"));
        DEFAULT_CONFIG.add(addFish("environmental:koi"));
        DEFAULT_CONFIG.add(addFish("betterendforge:end_fish"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:flying_fish"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:nautilus"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:eel_freshwater"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:eel_saltwater"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:lamprey"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:piranha"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:barracuda"));
        DEFAULT_CONFIG.add(addFish("rediscovered:fish", 1));
        //DEFAULT_VALUES.add(addDef("----"));


        for (List<String> d : DEFAULT_CONFIG) {
            if (d.size() == 5) {
                int f = strToInt(d.get(4));
                if (f > 0 && !d.get(0).equals("")) FISHES_2D.add(d.get(0));
            }
        }
        for(String id : FISHES_2D){
            BucketHelper.tryAddingFromEntityId(id);
        }
    }

    public static CapturedMobConfigProperties getTypeFromBucket(Item bucket) {
        EntityType<?> t = BucketHelper.getEntityType(bucket);
        if(t != null) return getType(t);
        return getType("minecraft:fish");
    }

    public static CapturedMobConfigProperties getType(Entity mob) {
        return getType(mob.getType());
    }
    public static CapturedMobConfigProperties getType(EntityType<?> type) {
        return getType(type.getRegistryName().toString());
    }

    public static CapturedMobConfigProperties getType(String mobId) {
        if(TYPES.containsKey(mobId)) return TYPES.get(mobId);
        //else if(BucketHelper.isBucketableEntity(mobId)) return getType("minecraft:fish");
        return TYPES.getOrDefault(mobId, DEFAULT);
    }

    private static float strToFloat(String s) {
        if (s != null && s.matches("[0-9.]+")) {
            return Float.parseFloat(s);
        }
        return 0;
    }

    private static int strToInt(String s) {
        if (s != null && s.matches("[0-9.]+")) {
            return Integer.parseInt(s);
        }
        return 0;
    }

    public static void refresh() {
        TYPES.clear();

        List<? extends List<String>> config = ConfigHandler.safeGetListString(ClientConfigs.CLIENT_SPEC, ClientConfigs.block.CAPTURED_MOBS_PROPERTIES);
        for (List<String> l : config) {
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
                        case "air" -> cat = AnimationCategory.AIR;
                        case "land" -> cat = AnimationCategory.LAND;
                        case "floating" -> cat = AnimationCategory.FLOATING;
                        default -> {
                            fish = strToInt(type);
                            if (fish > 0) cat = AnimationCategory.FISH;
                        }
                    }
                }
                //skip if mod isn't loaded
                if(!ModList.get().isLoaded(new ResourceLocation(id).getNamespace()) && cat != AnimationCategory.FISH) continue;
                /*
                if(size>5) {
                    addValidBucket(l.get(5));
                }else{
                    tryAddingValidBucket(id);
                }*/
                CapturedMobConfigProperties type = new CapturedMobConfigProperties(id, h, w, light, fish, cat);
                TYPES.put(id, type);
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("failed to load captured mob configs");
            }
        }
    }

    public static class CapturedMobConfigProperties {
        private final String id;
        private final float extraWidth;
        private final float extraHeight;
        private final int lightLevel;
        private final int fishTexture;
        private final AnimationCategory category;

        private CapturedMobConfigProperties(String id, float h, float w, int light, int fish, AnimationCategory c) {
            this.id = id;
            this.extraWidth = w;
            this.extraHeight = h;
            this.lightLevel = light;
            this.fishTexture = fish - 1;
            this.category = c;
        }

        public AnimationCategory getCategory() {
            return category;
        }

        public boolean canHaveWater() {
            return this.isFlying() || this.is2DFish();
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

        public boolean is2DFish() {
            return this.category.isFish();
        }

        public boolean isFlying() {
            return this.category.isFlying();
        }

        public boolean isLand() {
            return this.category.isLand();
        }

        public boolean isFloating() {
            return this.category.isFloating();
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public boolean canBe2dFish(){
            return this == DEFAULT || this.is2DFish();
        }
    }

    public enum AnimationCategory {
        DEFAULT,
        FISH,
        LAND,
        AIR,
        FLOATING;

        public boolean isFlying() {
            return this == AIR || this == FLOATING;
        }

        public boolean isLand() {
            return this == LAND;
        }

        public boolean isFloating() {
            return this == FLOATING;
        }

        public boolean isFish() {
            return this == FISH;
        }
    }



}
