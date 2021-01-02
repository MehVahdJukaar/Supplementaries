package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Calendar;
import java.util.UUID;

import static net.mehvahdjukaar.supplementaries.common.Resources.*;

public class CommonUtil {

    public static boolean ishalloween;
    public static boolean aprilfool;
    static{
        Calendar calendar = Calendar.getInstance();
        ishalloween= ((calendar.get(Calendar.MONTH)==Calendar.OCTOBER && calendar.get(Calendar.DATE)>=29)||
                        (calendar.get(Calendar.MONTH)== Calendar.NOVEMBER&&calendar.get(Calendar.DATE) <= 1));
        aprilfool= (calendar.get(Calendar.MONTH)==Calendar.APRIL&&calendar.get(Calendar.DATE)==1);
    }

    //TODO: I hope nobody is reading this


    //fluids
    public enum JarLiquidType {
        //TODO: move to config and add mod support
        // color is handles separately. here it's just for default case  FF6600
        WATER(WATER_TEXTURE, 0x3F76E4, true, 1f, true, true, false, -1),
        LAVA(LAVA_TEXTURE, 0xfd6d15, false, 1f, false, true, false, -1),
        MILK(MILK_TEXTURE, 0xFFFFFF, false, 1f, false, true, false, -1),
        POTION(POTION_TEXTURE, 0x3F76E4, true, 0.88f, true, false, false, -1),
        HONEY(HONEY_TEXTURE, 0xffa710, false, 0.85f, true, false, false, -1),
        DRAGON_BREATH(DRAGON_BREATH_TEXTURE, 0xFF33FF, true, 0.8f, true, false, false, -1),
        XP(XP_TEXTURE, 0x8eff11, false, 0.95f, true, false, false, -1),
        TROPICAL_FISH(WATER_TEXTURE, 0x3F76E4, true, 1f, false, true, false, 0),
        SALMON(WATER_TEXTURE, 0x3F76E4, true, 1f, false, true, false, 1),
        COD(WATER_TEXTURE, 0x3F76E4, true, 1f, false, true, false, 2),
        PUFFER_FISH(WATER_TEXTURE, 0x3F76E4, true, 1f, false, true, false, 3),
        COOKIES(WATER_TEXTURE, 0x000000, false, 1f, false, false, false, -1),
        EMPTY(WATER_TEXTURE, 0x000000, false, 1f, false, false, false, -1),
        MUSHROOM_STEW(SOUP_TEXTURE, 0xffad89, true, 1f, false, false, true, -1),
        BEETROOT_SOUP(SOUP_TEXTURE, 0xC93434, true, 1f, false, false, true, -1),
        SUSPICIOUS_STEW(SOUP_TEXTURE, 0xBAE85F, true, 1f, false, false, true, -1),
        RABBIT_STEW(SOUP_TEXTURE, 0xFF904F, true, 1f, false, false, true, -1);

        public final ResourceLocation texture;
        public final float opacity;
        public final int color;
        public final boolean applyColor;
        public final boolean bucket;
        public final boolean bottle;
        public final int fishType;
        public final boolean bowl;

        JarLiquidType(ResourceLocation texture, int color, boolean applycolor, float opacity, boolean bottle, boolean bucket, boolean bowl, int fishtype) {
            this.texture = texture;
            this.color = color; // beacon color. this will also be texture color if applycolor is true
            this.applyColor = applycolor; // is texture grayscale and needs to be colored?
            this.opacity = opacity;
            this.bottle = bottle;
            this.bucket = bucket;
            this.bowl = bowl;
            this.fishType = fishtype;
            // offset for fish textures. -1 is no fish
        }

        public boolean isEmpty(){
            return this==EMPTY;
        }

        public boolean isFish() {
            return this.fishType != -1;
        }

        public boolean isLava() {
            return this == JarLiquidType.LAVA;
        }

        public boolean isWater() {
            return this.isFish() || this == JarLiquidType.WATER;
        }

        public Item getReturnItem() {
            if (this.bottle)
                return Items.GLASS_BOTTLE;
            else if (this.bucket)
                return Items.BUCKET;
            else if (this.bowl)
                return Items.BOWL;
            return null;
        }

        public boolean makesSound() {
            return this.bottle || this.bowl || this.bucket;
        }

        //only for bucket
        public SoundEvent getSound() {
            if (this.isLava()) return SoundEvents.ITEM_BUCKET_FILL_LAVA;
            else if (this.isFish()) return SoundEvents.ITEM_BUCKET_FILL_FISH;
            else return SoundEvents.ITEM_BUCKET_FILL;
        }

    }

    public static JarLiquidType getJarContentTypeFromItem(ItemStack stack) {
        Item i = stack.getItem();
        if (i instanceof PotionItem) {
            if (PotionUtils.getPotionFromItem(stack).equals(Potions.WATER)) {
                return JarLiquidType.WATER;
            } else {
                return JarLiquidType.POTION;
            }
        } else if (i instanceof FishBucketItem) {
            if (i == Items.COD_BUCKET) {
                return JarLiquidType.COD;
            } else if (i == Items.PUFFERFISH_BUCKET) {
                return JarLiquidType.PUFFER_FISH;
            } else if (i == Items.SALMON_BUCKET) {
                return JarLiquidType.SALMON;
            } else {
                return JarLiquidType.TROPICAL_FISH;
            }
        } else if (i == Items.LAVA_BUCKET) {
            return JarLiquidType.LAVA;
        } else if (i instanceof HoneyBottleItem) {
            return JarLiquidType.HONEY;
        } else if (i instanceof MilkBucketItem) {
            return JarLiquidType.MILK;
        } else if (i == Items.DRAGON_BREATH) {
            return JarLiquidType.DRAGON_BREATH;
        } else if (i instanceof ExperienceBottleItem) {
            return JarLiquidType.XP;
        } else if (i == Items.MUSHROOM_STEW) {
            return JarLiquidType.MUSHROOM_STEW;
        } else if (i == Items.RABBIT_STEW) {
            return JarLiquidType.RABBIT_STEW;
        } else if (i == Items.BEETROOT_SOUP) {
            return JarLiquidType.BEETROOT_SOUP;
        } else if (i instanceof SuspiciousStewItem) {
            return JarLiquidType.SUSPICIOUS_STEW;
        } else if (i == Items.COOKIE) {
            return JarLiquidType.COOKIES;
        }else if (i == Items.WATER_BUCKET){
            return JarLiquidType.WATER;
        }
        return JarLiquidType.EMPTY;
    }

    //converts bucket and bowls in minecraft bottle fluid unit
    public static int getLiquidCountFromItem(Item i) {
        if (i instanceof FishBucketItem) return 1;
        if (i instanceof MilkBucketItem || i == Items.LAVA_BUCKET || i == Items.WATER_BUCKET) return 3;
        else if (i instanceof SoupItem || i instanceof SuspiciousStewItem) return 2;
        else return 1;
    }

    public enum WoodType implements IStringSerializable {
        NONE("none"),
        OAK("oak"),
        BIRCH("birch"),
        SPRUCE("spruce"),
        JUNGLE("jungle"),
        ACACIA("acacia"),
        DARK_OAK("dark_oak"),
        CRIMSON("crimson"),
        WARPED("warped");
        private final String name;

        WoodType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

    }

    //else if else if else if
    public static WoodType getWoodTypeFromSignPostItem(Item item) {
        if (item == Registry.SIGN_POST_ITEM_OAK) {
            return WoodType.OAK;
        } else if (item == Registry.SIGN_POST_ITEM_BIRCH) {
            return WoodType.BIRCH;
        } else if (item == Registry.SIGN_POST_ITEM_SPRUCE) {
            return WoodType.SPRUCE;
        } else if (item == Registry.SIGN_POST_ITEM_JUNGLE) {
            return WoodType.JUNGLE;
        } else if (item == Registry.SIGN_POST_ITEM_ACACIA) {
            return WoodType.ACACIA;
        } else if (item == Registry.SIGN_POST_ITEM_DARK_OAK) {
            return WoodType.DARK_OAK;
        } else if (item == Registry.SIGN_POST_ITEM_CRIMSON) {
            return WoodType.CRIMSON;
        } else if (item == Registry.SIGN_POST_ITEM_WARPED) {
            return WoodType.WARPED;
        }
        return WoodType.NONE;
    }

    public static Item getSignPostItemFromWoodType(WoodType wood) {
        switch (wood) {
            case OAK:
                return Registry.SIGN_POST_ITEM_OAK;
            case BIRCH:
                return Registry.SIGN_POST_ITEM_BIRCH;
            case SPRUCE:
                return Registry.SIGN_POST_ITEM_SPRUCE;
            case JUNGLE:
                return Registry.SIGN_POST_ITEM_JUNGLE;
            case ACACIA:
                return Registry.SIGN_POST_ITEM_ACACIA;
            case DARK_OAK:
                return Registry.SIGN_POST_ITEM_DARK_OAK;
            case CRIMSON:
                return Registry.SIGN_POST_ITEM_CRIMSON;
            case WARPED:
                return Registry.SIGN_POST_ITEM_WARPED;
            default:
            case NONE:
                return Items.AIR.getItem();
        }
    }

    //bounding box
    public static AxisAlignedBB getDirectionBB(BlockPos pos, Direction facing, int offset) {
        BlockPos endPos = pos.offset(facing, offset);
        switch (facing) {
            default:
            case NORTH:
                endPos = endPos.add(1, 1, 0);
                break;
            case SOUTH:
                endPos = endPos.add(1, 1, 1);
                pos = pos.add(0,0,1);
                break;
            case UP:
                endPos = endPos.add(1, 1, 1);
                pos = pos.add(0,1,0);
                break;
            case EAST:
                endPos = endPos.add(1, 1, 1);
                pos = pos.add(1,0,0);
                break;
            case WEST:
                endPos = endPos.add(0, 1, 1);
                break;
            case DOWN:
                endPos = endPos.add(1, 0, 1);
                break;
        }
        return new AxisAlignedBB(pos, endPos);
    }


    public enum GlobeType {
        DEFAULT(null, null, GLOBE_TEXTURE),
        FLAT(new String[]{"flat"}, new TranslationTextComponent("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE),
        MOON(new String[]{"moon","luna","selene","cynthia"},
                new TranslationTextComponent("globe.supplementaries.moon"),GLOBE_MOON_TEXTURE),
        EARTH(new String[]{"earth","terra","gaia","gaea","tierra","tellus","terre"},
                new TranslationTextComponent("globe.supplementaries.earth"),GLOBE_TEXTURE); //TODO: add via translationtext


        GlobeType(String[] k, TranslationTextComponent t, ResourceLocation r){
            this.keyWords = k;
            this.transKeyWord = t;
            this.texture = r;
        }

        public final String[] keyWords;
        public final TranslationTextComponent transKeyWord;
        public final ResourceLocation texture;

        public static GlobeType getGlobeType(String text){
            String name = text.toLowerCase();
            for (GlobeType n : GlobeType.values()) {
                if(n==DEFAULT)continue;
                if(name.contains(n.transKeyWord.getString().toLowerCase()))return n;
                for (String s : n.keyWords) {
                    if (name.contains(s)) {
                        return n;
                    }
                }
            }
            return GlobeType.DEFAULT;
        }

        public static GlobeType getGlobeType(TileEntity t){
            if(t instanceof INameable && ((INameable) t).hasCustomName()) {
                return getGlobeType(((INameable) t).getCustomName().toString());
            }
            return GlobeType.DEFAULT;
        }
    }


    public enum HourGlassSandType {
        DEFAULT(null,null,0),
        SAND(SAND_TEXTURE,"minecraft:sand", 60),
        RED_SAND(RED_SAND_TEXTURE,"minecraft:red_sand", 60),
        WHITE_CONCRETE(WHITE_CONCRETE_TEXTURE,"minecraft:white_concrete_powder", 90),
        ORANGE_CONCRETE(ORANGE_CONCRETE_TEXTURE,"minecraft:orange_concrete_powder", 90),
        LIGHT_BLUE_CONCRETE(LIGHT_BLUE_CONCRETE_TEXTURE,"minecraft:light_blue_concrete_powder", 90),
        YELLOW_CONCRETE(YELLOW_CONCRETE_TEXTURE,"minecraft:yellow_concrete_powder", 90),
        LIME_CONCRETE(LIME_CONCRETE_TEXTURE,"minecraft:lime_concrete_powder", 90),
        GREEN_CONCRETE(GREEN_CONCRETE_TEXTURE,"minecraft:green_concrete_powder",90),
        PINK_CONCRETE(PINK_CONCRETE_TEXTURE,"minecraft:pink_concrete_powder", 90),
        GRAY_CONCRETE(GRAY_CONCRETE_TEXTURE,"minecraft:gray_concrete_powder", 90),
        LIGHT_GRAY_CONCRETE(LIGHT_GRAY_CONCRETE_TEXTURE,"minecraft:light_gray_concrete_powder", 90),
        CYAN_CONCRETE(CYAN_CONCRETE_TEXTURE,"minecraft:cyan_concrete_powder", 90),
        PURPLE_CONCRETE(PURPLE_CONCRETE_TEXTURE,"minecraft:purple_concrete_powder", 90),
        BLUE_CONCRETE(BLUE_CONCRETE_TEXTURE,"minecraft:blue_concrete_powder", 90),
        BROWN_CONCRETE(BROWN_CONCRETE_TEXTURE,"minecraft:brown_concrete_powder", 90),
        RED_CONCRETE(RED_CONCRETE_TEXTURE,"minecraft:red_concrete_powder", 90),
        BLACK_CONCRETE(BLACK_CONCRETE_TEXTURE,"minecraft:black_concrete_powder", 90),
        MAGENTA_CONCRETE(MAGENTA_CONCRETE_TEXTURE,"minecraft:magenta_concrete_powder", 90),
        GUNPOWDER(HOURGLASS_GUNPOWDER,"minecraft:gunpowder", 150),
        SUGAR(HOURGLASS_SUGAR,"minecraft:sugar", 40),
        GLOWSTONE_DUST(HOURGLASS_GLOWSTONE,"minecraft:glowstone_dust", 120),
        REDSTONE_DUST(HOURGLASS_REDSTONE,"minecraft:redstone", 200),
        BLAZE_POWDER(HOURGLASS_BLAZE,"minecraft:blaze_powder", 100);



        public final ResourceLocation texture;
        public final String name;
        public final float increment;

        HourGlassSandType(ResourceLocation texture, String name, int t){
            this.texture = texture;
            this.name = name;
            this.increment =1f/(float)t;
        }
        public boolean isEmpty(){return this==DEFAULT;}

        public int getLight(){
            if(this==GLOWSTONE_DUST)return 9;
            if(this==BLAZE_POWDER)return 6;
            return 0;
        }

        public static HourGlassSandType getHourGlassSandType(Item i){
            String name = i.getRegistryName().toString();
            for (HourGlassSandType n : HourGlassSandType.values()){
                if(name.equals(n.name)){
                    return n;
                }
            }
            return HourGlassSandType.DEFAULT;
        }
    }


    //used for animation only (maybe caching item renderer later)
    public enum JarMobType {
        DEFAULT(null,0,0),
        SLIME("minecraft:slime",0,0),
        MAGMA_CUBE("minecraft:magma_cube",0,0),
        BEE("minecraft:bee",0.3125f,0),
        BAT("minecraft:bat",0,0),
        VEX("minecraft:vex",0,0.125f),
        ENDERMITE("minecraft:endermite",0,0),
        SILVERFISH("minecraft:silverfish",0,0.25f),
        PARROT("minecraft:parrot",0,0),
        CAT("minecraft:cat",0,0.1875f),
        RABBIT("minecraft:rabbit",0,0),
        CHICKEN("minecraft:chicken",0.25f,0.3125f),
        PIXIE("iceandfire:pixie",0,0);


        public final String type;
        public final float adjHeight;
        public final float adjWidth;

        JarMobType(String type, float h, float w){
            this.type = type;
            this.adjHeight =h;
            this.adjWidth = w;
        }

        public static JarMobType getJarMobType(Entity e){
            String name = e.getType().getRegistryName().toString();
            for (JarMobType n : JarMobType.values()){
                if(name.equals(n.type)){
                    return n;
                }
            }
            return JarMobType.DEFAULT;
        }
    }


    public static void createJarMobItemNBT(ItemStack stack, Entity mob, float blockh, float blockw){
        if(mob==null)return;
        if(mob instanceof LivingEntity){
            LivingEntity le = (LivingEntity) mob;
            le.prevRotationYawHead = 0;
            le.rotationYawHead = 0;
            le.limbSwingAmount = 0;
            le.prevLimbSwingAmount = 0;
            le.limbSwing = 0;
            le.hurtTime=0;
            le.maxHurtTime=0;
            le.hurtTime=0;
        }
        mob.rotationYaw = 0;
        mob.prevRotationYaw = 0;
        mob.prevRotationPitch = 0;
        mob.rotationPitch = 0;
        mob.extinguish();
        mob.hurtResistantTime=0;

        UUID id = mob.getUniqueID();

        CompoundNBT mobCompound = new CompoundNBT();
        mob.writeUnlessPassenger(mobCompound);
        if (!mobCompound.isEmpty()) {

            mobCompound.remove("Passengers");
            mobCompound.remove("Leash");
            mobCompound.remove("UUID");


            CompoundNBT cacheCompound = new CompoundNBT();

            boolean flag = mob.hasNoGravity() || mob instanceof IFlyingAnimal||mob.doesEntityNotTriggerPressurePlate();

            JarMobType type = JarMobType.getJarMobType(mob);
            float babyscale = 1;
            //non ageable

            if(mob instanceof AgeableEntity && ((AgeableEntity) mob).isChild()) babyscale = 2f;
            if(mobCompound.contains("IsBaby")&&mobCompound.getBoolean("IsBaby")||
                    (mob instanceof VillagerEntity && ((VillagerEntity) mob).isChild())) babyscale = 1.125f;

            float s = 1;
            float w = mob.getWidth() *babyscale;
            float h = mob.getHeight() *babyscale;
            //float maxh = flag ? 0.5f : 0.75f;
            //1 px border
            float maxh = blockh - (flag ? 0.25f : 0.125f) - type.adjHeight;
            float maxw = blockw - 0.25f - type.adjWidth;
            if (w > maxw || h > maxh) {
                if (w - maxw > h - maxh)
                    s = maxw / w;
                else
                    s = maxh / h;
            }
            //TODO: rewrite this to account for adjValues
            float y = flag ? (blockh/2f) - h * s / 2f : 0.0626f;

            //ice&fire dragons
            String name = mob.getType().getRegistryName().toString();
            if(name.equals("iceandfire:fire_dragon")||name.equals("iceandfire:ice_dragon")||name.equals("iceandfire:lightning_dragon")){
                s*=0.45;
            }

            cacheCompound.putFloat("Scale", s);
            cacheCompound.putFloat("YOffset", y);
            cacheCompound.putString("Name",mob.getName().getString());
            cacheCompound.putUniqueId("oldID",id);
            stack.setTagInfo("CachedJarMobValues", cacheCompound);
            stack.setTagInfo("JarMob", mobCompound);
        }
    }





}