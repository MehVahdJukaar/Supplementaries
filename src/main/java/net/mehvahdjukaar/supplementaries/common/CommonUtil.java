package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.Tags;

import java.util.Calendar;

import static net.mehvahdjukaar.supplementaries.common.Textures.*;

public class CommonUtil {

    public static void swapItem(PlayerEntity player, Hand hand, ItemStack oldItem, ItemStack newItem, boolean bothsides){
        if(!player.level.isClientSide || bothsides)
            player.setItemInHand(hand, DrinkHelper.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }

    public static void swapItem(PlayerEntity player, Hand hand, ItemStack oldItem, ItemStack newItem){
        if(!player.level.isClientSide)
        player.setItemInHand(hand, DrinkHelper.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }
    public static void swapItem(PlayerEntity player, Hand hand, ItemStack newItem){
        if(!player.level.isClientSide)
        player.setItemInHand(hand, DrinkHelper.createFilledResult(player.getItemInHand(hand).copy(), player, newItem, player.isCreative()));
    }

    public enum Festivity{
        NONE,
        HALLOWEEN,
        APRILS_FOOL,
        CHRISTMAS,
        EARTH_DAY,
        ST_VALENTINE;
        public boolean isHalloween(){return this==HALLOWEEN;}
        public boolean isAprilsFool(){return this==APRILS_FOOL;}
        public boolean isStValentine(){return this==ST_VALENTINE;}
        public boolean isChristmas(){return this==CHRISTMAS;}
        public boolean isEarthDay(){return this==EARTH_DAY;}
        public static Festivity get(){
            Calendar calendar = Calendar.getInstance();
            if((calendar.get(Calendar.MONTH)==Calendar.OCTOBER && calendar.get(Calendar.DATE)>=29)||
                    (calendar.get(Calendar.MONTH)== Calendar.NOVEMBER&&calendar.get(Calendar.DATE) <= 1))return HALLOWEEN;
            if(calendar.get(Calendar.MONTH)==Calendar.APRIL&&calendar.get(Calendar.DATE)==1)return APRILS_FOOL;
            if(calendar.get(Calendar.MONTH)==Calendar.FEBRUARY&&calendar.get(Calendar.DATE)==14)return ST_VALENTINE;
            if(calendar.get(Calendar.MONTH)==Calendar.APRIL&&calendar.get(Calendar.DATE)==22)return EARTH_DAY;
            if(calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26)return CHRISTMAS;
            return NONE;
        }

    }

    public static Festivity FESTIVITY = Festivity.get();


    //TODO: I hope nobody is reading this


    //fluids
    public enum JarLiquidType {
        //TODO: move to config and add mod support
        // color is handles separately. here it's just for default case  FF6600
        WATER(WATER_TEXTURE, 0x3F76E4, true, 1f, true, true, false, -1),
        LAVA(LAVA_TEXTURE, 0xfd6d15, false, 1f, false, true, false, -1),
        MILK(MILK_TEXTURE, 0xFFFFFF, false, 1f, false, true, false, -1),
        POTION(POTION_TEXTURE_FLOW, 0x3F76E4, true, 0.88f, true, false, false, -1),
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

        public int getLightLevel(){
            return this.isLava()?15:0;
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
            if (this.isLava()) return SoundEvents.BUCKET_FILL_LAVA;
            else if (this.isFish()) return SoundEvents.BUCKET_FILL_FISH;
            else return SoundEvents.BUCKET_FILL;
        }

        public SoundEvent getEatSound() {
            return this==COOKIES?SoundEvents.GENERIC_EAT:SoundEvents.GENERIC_DRINK;
        }

    }

    public static boolean isCookie(Item i){
        return (ModTags.isTagged(ModTags.COOKIES,i));
    }

    //TODO: move to tag
    public static boolean isLantern(Item i){
        if(i instanceof BlockItem){
            Block b =  ((BlockItem) i).getBlock();
            String namespace = b.getRegistryName().getNamespace();
            if(namespace.equals("skinnedlanterns")) return true;
            return (b instanceof LanternBlock && !ServerConfigs.cached.WALL_LANTERN_BLACKLIST.contains(namespace));
        }
        return false;
    }

    public static boolean isBrick(Item i){
        return (ModTags.isTagged(ModTags.BRICKS,i));
    }

    public static boolean isCake(Item i){
        if(i instanceof BlockItem){
            Block b = ((BlockItem) i).getBlock();
            return ((b == Blocks.CAKE)||b == Registry.DIRECTIONAL_CAKE.get());
        }
        return false;
    }

    public static boolean isPot(Item i){
        if(i instanceof BlockItem){
            Block b =  ((BlockItem) i).getBlock();
            //String namespace = b.getRegistryName().getNamespace();
            return ((b instanceof FlowerPotBlock));
        }
        return false;
    }


    public static JarLiquidType getJarContentTypeFromItem(ItemStack stack) {
        Item i = stack.getItem();
        if (i instanceof PotionItem) {
            if (PotionUtils.getPotion(stack).equals(Potions.WATER)) {
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
        } else if (i == Items.WATER_BUCKET){
            return JarLiquidType.WATER;
        } else if (isCookie(i)) {
            return JarLiquidType.COOKIES;
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

    public enum TempWoodType implements IStringSerializable {
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

        TempWoodType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public IWoodType convertWoodType(){
            switch (this){
                default:
                case OAK:
                    return VanillaWoodTypes.OAK;
                case BIRCH:
                    return VanillaWoodTypes.BIRCH;
                case SPRUCE:
                    return VanillaWoodTypes.SPRUCE;
                case JUNGLE:
                    return VanillaWoodTypes.JUNGLE;
                case DARK_OAK:
                    return VanillaWoodTypes.DARK_OAK;
                case WARPED:
                    return VanillaWoodTypes.WARPED;
                case CRIMSON:
                    return VanillaWoodTypes.CRIMSON;
                case ACACIA:
                    return VanillaWoodTypes.ACACIA;
            }
        }

    }


    //bounding box
    public static AxisAlignedBB getDirectionBB(BlockPos pos, Direction facing, int offset) {
        BlockPos endPos = pos.relative(facing, offset);
        switch (facing) {
            default:
            case NORTH:
                endPos = endPos.offset(1, 1, 0);
                break;
            case SOUTH:
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(0,0,1);
                break;
            case UP:
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(0,1,0);
                break;
            case EAST:
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(1,0,0);
                break;
            case WEST:
                endPos = endPos.offset(0, 1, 1);
                break;
            case DOWN:
                endPos = endPos.offset(1, 0, 1);
                break;
        }
        return new AxisAlignedBB(pos, endPos);
    }




    //equals is not working...
    public static boolean isShapeEqual(AxisAlignedBB s1, AxisAlignedBB s2){
        return s1.minX==s2.minX&&s1.minY==s2.minY&&s1.minZ==s2.minZ&&s1.maxX==s2.maxX&&s1.maxY==s2.maxY&&s1.maxZ==s2.maxZ;
    }
    public static final AxisAlignedBB FENCE_SHAPE = Block.box(6,0,6,10,16,10).bounds();
    public static final AxisAlignedBB POST_SHAPE = Block.box(5,0,5,11,16,11).bounds();
    public static final AxisAlignedBB WALL_SHAPE = Block.box(7,0,7,12,16,12).bounds();

    //0 normal, 1 fence, 2 walls TODO: change 1 with 2
    public static int getPostSize(BlockState state, BlockPos pos, IWorldReader world){
        Block block = state.getBlock();

        VoxelShape shape = state.getShape(world, pos);
        if(shape!= VoxelShapes.empty()) {
            AxisAlignedBB s = shape.bounds();
            if (block instanceof FenceBlock || block instanceof SignPostBlock || block.is(Tags.Blocks.FENCES) || isShapeEqual(FENCE_SHAPE, s))
                return 1;
            if (block instanceof WallBlock || block.is(BlockTags.WALLS) ||
                    (isShapeEqual(WALL_SHAPE, s))) return 2;
            if (isShapeEqual(POST_SHAPE, s)) return 1;
        }

        return 0;
    }

    public static boolean isVertical(BlockState state){
        if(state.hasProperty(BlockStateProperties.AXIS)){
            return state.getValue(BlockStateProperties.AXIS)== Direction.Axis.Y;
        }
        return true;
    }

    //for quarks hedges
    public static boolean isNotExtended(BlockState state){
        if(state.hasProperty(BlockStateProperties.EXTENDED)){
            return !state.getValue(BlockStateProperties.EXTENDED);
        }
        return true;
    }

    public static boolean isPost(BlockState state){
        return isVertical(state) && isNotExtended(state) && ModTags.isTagged(ModTags.POSTS,state.getBlock());
    }

    //this is how you do it :D
    private static final ShulkerBoxTileEntity SHULKER_TILE = new ShulkerBoxTileEntity();
    public static boolean isAllowedInShulker(ItemStack stack){
        return SHULKER_TILE.canPlaceItemThroughFace(0,stack,null);
    }


    //cylinder distance
    public static boolean withinDistanceDown(BlockPos pos, Vector3d vector, double distW, double distDown) {
        double dx = vector.x() - ((double)pos.getX() + 0.5);
        double dy = vector.y() - ((double)pos.getY() + 0.5);
        double dz = vector.z() - ((double)pos.getZ() + 0.5);
        double mydistW = (dx*dx + dz*dz);
        return (mydistW<(distW*distW) && (dy<distW && dy>-distDown));
    }


    //BlockItem method
    public static boolean canPlace(BlockItemUseContext context, BlockState state){
        PlayerEntity playerentity = context.getPlayer();
        ISelectionContext iselectioncontext = playerentity == null ? ISelectionContext.empty() : ISelectionContext.of(playerentity);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) && context.getLevel().isUnobstructed(state, context.getClickedPos(), iselectioncontext);
    }




}