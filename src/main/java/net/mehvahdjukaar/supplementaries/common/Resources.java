package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resources {


    //TODO: I hope nobody is reading this

    //blockstate properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final IntegerProperty EXTENSION = IntegerProperty.create("extension", 0, 2);
    public static final BooleanProperty TILE = BooleanProperty.create("tile");
    public static final IntegerProperty TILE_3 = IntegerProperty.create("tile_3", 0, 2);
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_JAR = BooleanProperty.create("has_jar");
    public static final EnumProperty<CommonUtil.WoodType> WOOD_TYPE = EnumProperty.create("wood_type", CommonUtil.WoodType.class);
    // it's detecting incoming laser and its distance
    public static final IntegerProperty RECEIVING = IntegerProperty.create("laser_receiving", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");


    //sounds
    //TODO: add alot more
    public static final ResourceLocation TICK_1 = new ResourceLocation("supplementaries:tick_1");
    public static final ResourceLocation TICK_2 = new ResourceLocation(Supplementaries.MOD_ID, "tick_2");


    //textures
    public static final ResourceLocation WHITE_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/white_concrete_powder");
    public static final ResourceLocation ORANGE_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/orange_concrete_powder");
    public static final ResourceLocation LIGHT_BLUE_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/light_blue_concrete_powder");
    public static final ResourceLocation YELLOW_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/yellow_concrete_powder");
    public static final ResourceLocation LIME_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/lime_concrete_powder");
    public static final ResourceLocation PINK_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/pink_concrete_powder");
    public static final ResourceLocation GRAY_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/gray_concrete_powder");
    public static final ResourceLocation LIGHT_GRAY_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/light_gray_concrete_powder");
    public static final ResourceLocation CYAN_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/cyan_concrete_powder");
    public static final ResourceLocation PURPLE_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/purple_concrete_powder");
    public static final ResourceLocation BLUE_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/blue_concrete_powder");
    public static final ResourceLocation BROWN_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/brown_concrete_powder");
    public static final ResourceLocation GREEN_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/green_concrete_powder");
    public static final ResourceLocation RED_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/red_concrete_powder");
    public static final ResourceLocation BLACK_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/black_concrete_powder");
    public static final ResourceLocation MAGENTA_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/magenta_concrete_powder");
    public static final ResourceLocation RED_SAND_TEXTURE = new ResourceLocation("minecraft:block/red_sand");
    public static final ResourceLocation SAND_TEXTURE = new ResourceLocation("minecraft:block/sand");
    public static final ResourceLocation WATER_TEXTURE = new ResourceLocation("minecraft:block/water_still");
    public static final ResourceLocation LAVA_TEXTURE = new ResourceLocation("minecraft:block/lava_still");
    public static final ResourceLocation MILK_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/milk_liquid");

    public static final ResourceLocation POTION_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/potion_liquid");
    public static final ResourceLocation HONEY_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/honey_liquid");
    public static final ResourceLocation DRAGON_BREATH_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/dragon_breath_liquid");
    public static final ResourceLocation XP_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/xp_liquid");
    public static final ResourceLocation FAUCET_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/faucet_water");
    public static final ResourceLocation FISHIES_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/fishies");
    public static final ResourceLocation BELLOWS_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/bellows");
    public static final ResourceLocation LASER_BEAM_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/laser_beam");
    public static final ResourceLocation LASER_OVERLAY_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/laser_overlay");
    public static final ResourceLocation LASER_BEAM_END_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/laser_beam_end");
    public static final ResourceLocation SOUP_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/soup_liquid");
    public static final ResourceLocation FIREFLY_TEXTURE =  new ResourceLocation(Supplementaries.MOD_ID+":textures/entity/firefly.png");
    public static final ResourceLocation CLOCK_HAND_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/clock_hand");
    public static final ResourceLocation GLOBE_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID+":textures/entity/globe_the_world.png");
    public static final ResourceLocation GLOBE_FLAT_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID+":textures/entity/globe_flat.png");
    public static final ResourceLocation GLOBE_MOON_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID+":textures/entity/globe_moon.png");
    public static final ResourceLocation HOURGLASS_REDSTONE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/hourglass_redstone");
    public static final ResourceLocation HOURGLASS_GLOWSTONE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/hourglass_glowstone");
    public static final ResourceLocation HOURGLASS_SUGAR = new ResourceLocation(Supplementaries.MOD_ID, "blocks/hourglass_sugar");
    public static final ResourceLocation HOURGLASS_BLAZE = new ResourceLocation(Supplementaries.MOD_ID, "blocks/hourglass_blaze");
    public static final ResourceLocation HOURGLASS_GUNPOWDER = new ResourceLocation(Supplementaries.MOD_ID, "blocks/hourglass_gunpowder");

    public static final ResourceLocation NOTICE_BOARD_GUI_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID+":textures/gui/notice_board_gui.png");
    public static final ResourceLocation SACK_GUI_TEXTURE = new ResourceLocation(Supplementaries.MOD_ID+":textures/gui/sack_gui.png");



    public static List<ResourceLocation> getTextures() {
        return new ArrayList<>(Arrays.asList(MILK_TEXTURE, POTION_TEXTURE, HONEY_TEXTURE, DRAGON_BREATH_TEXTURE, SOUP_TEXTURE,
                XP_TEXTURE, FAUCET_TEXTURE, FISHIES_TEXTURE, BELLOWS_TEXTURE, LASER_BEAM_TEXTURE, LASER_BEAM_END_TEXTURE,LASER_OVERLAY_TEXTURE,
                CLOCK_HAND_TEXTURE, HOURGLASS_REDSTONE, HOURGLASS_GLOWSTONE, HOURGLASS_SUGAR, HOURGLASS_BLAZE, HOURGLASS_GUNPOWDER));
    }





}