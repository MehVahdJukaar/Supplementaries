package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile.BookColor;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Variants;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;

public class Textures {

    private static final String MOD_ID = Supplementaries.MOD_ID;
    private static final String LIB = Selene.MOD_ID;

    //minecraft
    public static final ResourceLocation DIRT_TEXTURE = new ResourceLocation("minecraft:block/dirt");
    public static final ResourceLocation WHITE_CONCRETE_TEXTURE = new ResourceLocation("minecraft:block/white_concrete_powder");
    public static final ResourceLocation SAND_TEXTURE = new ResourceLocation("minecraft:block/sand");
    public static final ResourceLocation WATER_TEXTURE = new ResourceLocation("minecraft:block/water_still");
    public static final ResourceLocation LAVA_TEXTURE = new ResourceLocation("minecraft:block/lava_still");
    public static final ResourceLocation CHAIN_TEXTURE = new ResourceLocation("minecraft:block/chain");
    public static final ResourceLocation FLOWING_WATER_TEXTURE = new ResourceLocation("minecraft:block/water_flow");
    public static final ResourceLocation SLIME_TEXTURE = new ResourceLocation("minecraft:block/slime_block");
    public static final ResourceLocation BLACKBOARD_TEXTURE = new ResourceLocation(MOD_ID + ":blocks/blackboard");

    //fluids

    public static final ResourceLocation MILK_TEXTURE = new ResourceLocation(LIB, "blocks/milk_liquid");
    public static final ResourceLocation POTION_TEXTURE = new ResourceLocation(LIB, "blocks/potion_still");
    public static final ResourceLocation POTION_TEXTURE_FLOW = new ResourceLocation(LIB, "blocks/potion_flow");
    public static final ResourceLocation HONEY_TEXTURE = new ResourceLocation(LIB, "blocks/honey_liquid");
    public static final ResourceLocation DRAGON_BREATH_TEXTURE = new ResourceLocation(LIB, "blocks/dragon_breath_liquid");
    public static final ResourceLocation SOUP_TEXTURE = new ResourceLocation(LIB, "blocks/soup_liquid");
    public static final ResourceLocation XP_TEXTURE = new ResourceLocation(LIB, "blocks/xp_still");
    public static final ResourceLocation XP_TEXTURE_FLOW = new ResourceLocation(LIB, "blocks/xp_flow");
    public static final ResourceLocation MAGMA_TEXTURE = new ResourceLocation(LIB, "blocks/magma_still");
    public static final ResourceLocation MAGMA_TEXTURE_FLOW = new ResourceLocation(LIB, "blocks/magma_flow");
    //blocks (to stitch)
    public static final ResourceLocation SUGAR_TEXTURE = Supplementaries.res("blocks/sugar");
    public static final ResourceLocation SOUL_TEXTURE = Supplementaries.res("blocks/soul");
    public static final ResourceLocation FISHIES_TEXTURE = Supplementaries.res("blocks/fishies");
    public static final ResourceLocation BELLOWS_TEXTURE = Supplementaries.res("entity/bellows");
    public static final ResourceLocation LASER_BEAM_TEXTURE = Supplementaries.res("blocks/laser_beam");
    public static final ResourceLocation LASER_OVERLAY_TEXTURE = Supplementaries.res("blocks/laser_overlay");
    public static final ResourceLocation LASER_BEAM_END_TEXTURE = Supplementaries.res("blocks/laser_beam_end");
    public static final ResourceLocation CLOCK_HAND_TEXTURE = Supplementaries.res("blocks/clock_hand");
    public static final ResourceLocation CRIMSON_LANTERN_TEXTURE = Supplementaries.res("blocks/crimson_lantern_front");
    public static final ResourceLocation HOURGLASS_REDSTONE = Supplementaries.res("blocks/hourglass_redstone");
    public static final ResourceLocation HOURGLASS_GLOWSTONE = Supplementaries.res("blocks/hourglass_glowstone");
    public static final ResourceLocation HOURGLASS_SUGAR = Supplementaries.res("blocks/hourglass_sugar");
    public static final ResourceLocation HOURGLASS_BLAZE = Supplementaries.res("blocks/hourglass_blaze");
    public static final ResourceLocation HOURGLASS_GUNPOWDER = Supplementaries.res("blocks/hourglass_gunpowder");
    public static final ResourceLocation BLACKBOARD_GRID = Supplementaries.res("blocks/blackboard_grid");

    public static final ResourceLocation TIMBER_FRAME_TEXTURE = Supplementaries.res("blocks/timber_frame");
    public static final ResourceLocation TIMBER_BRACE_TEXTURE = Supplementaries.res("blocks/timber_brace");
    public static final ResourceLocation TIMBER_BRACE_F_TEXTURE = Supplementaries.res("blocks/timber_brace_f");
    public static final ResourceLocation TIMBER_CROSS_BRACE_TEXTURE = Supplementaries.res("blocks/timber_cross_brace");
    //entities
    public static final ResourceLocation GLOBE_TEXTURE = Supplementaries.res("textures/entity/globes/globe_the_world.png");
    public static final ResourceLocation GLOBE_FLAT_TEXTURE = Supplementaries.res("textures/entity/globes/globe_flat.png");
    public static final ResourceLocation GLOBE_MOON_TEXTURE = Supplementaries.res("textures/entity/globes/globe_moon.png");
    public static final ResourceLocation GLOBE_SUN_TEXTURE = Supplementaries.res("textures/entity/globes/globe_sun.png");
    public static final ResourceLocation GLOBE_SHEARED_TEXTURE = Supplementaries.res("textures/entity/globes/globe_sheared.png");


    public static final ResourceLocation AMETHYST_ARROW = Supplementaries.res("textures/entity/amethyst_arrow.png");
    public static final ResourceLocation ROPE_ARROW = Supplementaries.res("textures/entity/rope_arrow.png");
    public static final ResourceLocation RED_MERCHANT = Supplementaries.res("textures/entity/misc/red_merchant.png");
    public static final ResourceLocation STATUE = Supplementaries.res("textures/entity/statue.png");

    public static final ResourceLocation FIREFLY_TEXTURE = Supplementaries.res("textures/entity/firefly.png");
    public static final ResourceLocation BELL_ROPE_TEXTURE = Supplementaries.res("textures/entity/bell_rope.png");
    public static final ResourceLocation BELL_CHAIN_TEXTURE = Supplementaries.res("textures/entity/bell_chain.png");
    public static final ResourceLocation THICK_GOLEM = Supplementaries.res("textures/entity/misc/iron_golem.png");
    public static final ResourceLocation SEA_PICKLE_RICK = Supplementaries.res("textures/entity/misc/sea_pickle.png");
    public static final ResourceLocation JAR_MAN = Supplementaries.res("textures/entity/misc/jar_man.png");


    //gui
    public static final ResourceLocation BLACKBOARD_GUI_TEXTURE = Supplementaries.res("textures/gui/blackboard.png");
    public static final ResourceLocation CONFIG_BACKGROUND = Supplementaries.res("textures/gui/config_background.png");
    public static final ResourceLocation NOTICE_BOARD_GUI_TEXTURE = Supplementaries.res("textures/gui/notice_board_gui.png");
    public static final ResourceLocation SACK_GUI_TEXTURE = Supplementaries.res("textures/gui/sack_gui.png");
    public static final ResourceLocation SLOT_TEXTURE = Supplementaries.res("textures/gui/slot.png");
    public static final ResourceLocation PULLEY_BLOCK_GUI_TEXTURE = Supplementaries.res("textures/gui/pulley_block_gui.png");
    public static final ResourceLocation PRESENT_BLOCK_GUI_TEXTURE = Supplementaries.res("textures/gui/present_block_gui.png");
    public static final ResourceLocation ORANGE_MERCHANT_GUI_TEXTURE = Supplementaries.res("textures/gui/orange_trader.png");
    //map markers
    public static final ResourceLocation SIGN_POST_MARKER_TEXTURE = Supplementaries.res("textures/map/sign_post.png");
    public static final ResourceLocation FLAG_MARKER_TEXTURE = Supplementaries.res("textures/map/flag.png");
    public static final ResourceLocation BED_MARKER_TEXTURE = Supplementaries.res("textures/map/bed.png");
    public static final ResourceLocation RESPAWN_ANCHOR_MARKER_TEXTURE = Supplementaries.res("textures/map/respawn_anchor.png");
    public static final ResourceLocation LODESTONE_MARKER_TEXTURE = Supplementaries.res("textures/map/lodestone.png");
    public static final ResourceLocation BEACON_MARKER_TEXTURE = Supplementaries.res("textures/map/beacon.png");
    public static final ResourceLocation CONDUIT_MARKER_TEXTURE = Supplementaries.res("textures/map/conduit.png");
    public static final ResourceLocation NETHER_PORTAL_MARKER_TEXTURE = Supplementaries.res("textures/map/nether_portal.png");
    public static final ResourceLocation END_PORTAL_MARKER_TEXTURE = Supplementaries.res("textures/map/end_portal.png");
    public static final ResourceLocation END_GATEWAY_MARKER_TEXTURE = Supplementaries.res("textures/map/end_gateway.png");

    public static final Map<IWoodType, ResourceLocation> HANGING_SIGNS_TEXTURES = new HashMap<>();
    public static final Map<IWoodType, ResourceLocation> SIGN_POSTS_TEXTURES = new HashMap<>();
    public static final Map<BannerPattern, ResourceLocation> FLAG_TEXTURES = new HashMap<>();
    public static final Map<BookColor, ResourceLocation> BOOK_TEXTURES = new HashMap<>();
    public static final ResourceLocation BOOK_ENCHANTED_TEXTURES = Supplementaries.res("entity/books/book_enchanted");
    public static final ResourceLocation BOOK_TOME_TEXTURES = Supplementaries.res("entity/books/book_tome");

    public static final ResourceLocation ANTIQUE_FONT = Supplementaries.res("antique");

    static {
        for (IWoodType type : WoodTypes.TYPES.values()) {
            HANGING_SIGNS_TEXTURES.put(type, Supplementaries.res("entity/hanging_signs/" + type.getLocation() + Variants.getHangingSignName(type)));
            SIGN_POSTS_TEXTURES.put(type, Supplementaries.res("entity/sign_posts/" + type.getLocation() + Variants.getSignPostName(type)));
        }
        Field f = ObfuscationReflectionHelper.findField(BannerPattern.class, "field_191014_N");
        for (BannerPattern pattern : BannerPattern.values()) {
            try {
                //get name isn't server side, apparently...
                f.setAccessible(true);
                String name = (String) f.get(pattern);
                FLAG_TEXTURES.put(pattern, Supplementaries.res("entity/flags/" + name));
            } catch (Exception ignored) {
            }
        }

        for (BookColor color : BookColor.values()) {
            BOOK_TEXTURES.put(color, Supplementaries.res("entity/books/book_" + color.getName()));
        }
    }


    //TODO: rethink this
    public static List<ResourceLocation> getTexturesToStitch() {
        return new ArrayList<>(Arrays.asList(
                FISHIES_TEXTURE, BELLOWS_TEXTURE, LASER_BEAM_TEXTURE, LASER_BEAM_END_TEXTURE, LASER_OVERLAY_TEXTURE,
                SUGAR_TEXTURE, CLOCK_HAND_TEXTURE, HOURGLASS_REDSTONE, HOURGLASS_GLOWSTONE, HOURGLASS_SUGAR, HOURGLASS_BLAZE,
                HOURGLASS_GUNPOWDER, BLACKBOARD_GRID));
    }


}