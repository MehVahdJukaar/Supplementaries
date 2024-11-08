package net.mehvahdjukaar.supplementaries.reg;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

//Needed on both sides because...
public class ModTextures {

    //minecraft
    public static final ResourceLocation WHITE_CONCRETE_TEXTURE = ResourceLocation.withDefaultNamespace("block/white_concrete_powder");
    public static final ResourceLocation SAND_TEXTURE = ResourceLocation.withDefaultNamespace("block/sand");
    public static final ResourceLocation CHAIN_TEXTURE = ResourceLocation.withDefaultNamespace("block/chain");
    public static final ResourceLocation HONEY_TEXTURE = ResourceLocation.withDefaultNamespace("block/honey_block_side");
    public static final ResourceLocation SLIME_TEXTURE = ResourceLocation.withDefaultNamespace("block/slime_block");


    public static final ResourceLocation FISHIES_TEXTURE = Supplementaries.res("block/fishies");
    public static final ResourceLocation BELLOWS_TEXTURE = Supplementaries.res("block/bellows");
    public static final ResourceLocation CLOCK_HAND_TEXTURE = Supplementaries.res("block/clock_hand");
    public static final ResourceLocation HOURGLASS_REDSTONE = Supplementaries.res("block/hourglass_redstone");
    public static final ResourceLocation HOURGLASS_GLOWSTONE = Supplementaries.res("block/hourglass_glowstone");
    public static final ResourceLocation HOURGLASS_BLAZE = Supplementaries.res("block/hourglass_blaze");
    public static final ResourceLocation HOURGLASS_GUNPOWDER = Supplementaries.res("block/hourglass_gunpowder");
    public static final ResourceLocation BLACKBOARD_GRID = Supplementaries.res("block/blackboard_grid");
    public static final ResourceLocation CANNON_TEXTURE = Supplementaries.res("block/cannon");


    public static final ResourceLocation SUGAR = Supplementaries.res("block/sugar");
    public static final ResourceLocation ASH = Supplementaries.res("block/ash");
    public static final ResourceLocation TIMBER_CROSS_BRACE_TEXTURE = Supplementaries.res("block/timber_cross_brace");
    public static final ResourceLocation BLACKBOARD_TEXTURE = Supplementaries.res("block/blackboard");
    public static final ResourceLocation BLACKBOARD_WHITE_TEXTURE = Supplementaries.res("block/blackboard_white");
    public static final ResourceLocation BLACKBOARD_BLACK_TEXTURE = Supplementaries.res("block/blackboard_black");

    //entities
    public static final ResourceLocation GLOBE_TEXTURE = Supplementaries.res("textures/entity/globes/globe_the_world.png");
    public static final ResourceLocation GLOBE_FLAT_TEXTURE = Supplementaries.res("textures/entity/globes/globe_flat.png");
    public static final ResourceLocation GLOBE_MOON_TEXTURE = Supplementaries.res("textures/entity/globes/globe_moon.png");
    public static final ResourceLocation GLOBE_SUN_TEXTURE = Supplementaries.res("textures/entity/globes/globe_sun.png");
    public static final ResourceLocation GLOBE_SHEARED_TEXTURE = Supplementaries.res("textures/entity/globes/globe_sheared.png");
    public static final ResourceLocation GLOBE_SHEARED_SEPIA_TEXTURE = Supplementaries.res("textures/entity/globes/globe_sheared_sepia.png");
    public static final ResourceLocation PARTY_CREEPER = Supplementaries.res("textures/item/party_hat.png");
    public static final ResourceLocation ENDERMAN_HEAD = Supplementaries.res("textures/entity/enderman_head.png");
    public static final ResourceLocation ENDERMAN_HEAD_EYES = Supplementaries.res("textures/entity/enderman_head_eyes.png");
    public static final ResourceLocation CANNON_TRAJECTORY = Supplementaries.res("textures/entity/cannon_trajectory.png");
    public static final ResourceLocation CANNON_TRAJECTORY_RED = Supplementaries.res("textures/entity/cannon_trajectory_charging.png");
    public static final ResourceLocation CANNONBALL_TEXTURE = Supplementaries.res("textures/entity/cannonball.png");


    public static final ResourceLocation ROPE_ARROW = Supplementaries.res("textures/entity/rope_arrow.png");
    public static final ResourceLocation RED_MERCHANT = Supplementaries.res("textures/entity/misc/red_merchant.png");
    public static final ResourceLocation ORANGE_MERCHANT = Supplementaries.res("textures/entity/misc/orange_merchant.png");
    public static final ResourceLocation RED_MERCHANT_CHRISTMAS = Supplementaries.res("textures/entity/misc/christmas_merchant.png");
    public static final ResourceLocation STATUE = Supplementaries.res("textures/entity/statue.png");
    public static final ResourceLocation HAT_STAND = Supplementaries.res("textures/entity/hat_stand.png");

    public static final ResourceLocation FIREFLY_TEXTURE = Supplementaries.res("textures/entity/firefly.png");
    public static final ResourceLocation BELL_ROPE_TEXTURE = Supplementaries.res("textures/entity/bell_rope.png");
    public static final ResourceLocation BELL_CHAIN_TEXTURE = Supplementaries.res("textures/entity/bell_chain.png");
    public static final ResourceLocation THICK_GOLEM = Supplementaries.res("textures/entity/misc/iron_golem.png");
    public static final ResourceLocation SEA_PICKLE_RICK = Supplementaries.res("textures/entity/misc/sea_pickle.png");
    public static final ResourceLocation JAR_MAN = Supplementaries.res("textures/entity/misc/jar_man.png");
    public static final ResourceLocation SLIME_ENTITY_OVERLAY = Supplementaries.res("textures/entity/slime_overlay.png");

    public static final ResourceLocation ANTIQUABLE_FONT = Supplementaries.res("antiquable");

    //gui
    public static final ResourceLocation SLIME_GUI_OVERLAY = Supplementaries.res("textures/gui/slime_overlay.png");
    public static final ResourceLocation BLACKBOARD_GUI_TEXTURE = Supplementaries.res("textures/gui/blackboard.png");
    public static final ResourceLocation CONFIG_BACKGROUND = Supplementaries.res("textures/gui/config_background.png");
    public static final ResourceLocation NOTICE_BOARD_GUI_TEXTURE = Supplementaries.res("textures/gui/notice_board_gui.png");
    public static final ResourceLocation VARIABLE_SIZE_CONTAINER_TEXTURE = Supplementaries.res("textures/gui/variable_size_container_gui.png");
    public static final ResourceLocation SLOT_TEXTURE = Supplementaries.res("textures/gui/slot.png");
    public static final ResourceLocation PULLEY_BLOCK_GUI_TEXTURE = Supplementaries.res("textures/gui/pulley_block_gui.png");
    public static final ResourceLocation PRESENT_GUI_TEXTURE = Supplementaries.res("textures/gui/present_gui.png");
    public static final ResourceLocation PRESENT_EMPTY_GUI_TEXTURE = Supplementaries.res("textures/gui/present_empty_gui.png");
    public static final ResourceLocation TRAPPED_PRESENT_GUI_TEXTURE = Supplementaries.res("textures/gui/trapped_present_gui.png");
    public static final ResourceLocation CANNON_GUI_TEXTURE = Supplementaries.res("textures/gui/cannon_gui.png");
    public static final ResourceLocation RED_MERCHANT_GUI_TEXTURE = Supplementaries.res("textures/gui/red_merchant.png");
    public static final ResourceLocation TATTERED_BOOK_GUI_TEXTURE = Supplementaries.res("textures/gui/tattered_book.png");
    public static final ResourceLocation CANNON_ICONS_TEXTURE = Supplementaries.res("textures/gui/cannon_icons.png");
    public static final ResourceLocation QUIVER_HUD = Supplementaries.res("textures/gui/quiver_hud.png");
    public static final ResourceLocation QUIVER_TOOLTIP = ResourceLocation.withDefaultNamespace("container/bundle/background");

    // sprites
    public static final ResourceLocation BLACKBOARD_DYE_SPRITE = Supplementaries.res("blackboard/dye_button");
    public static final ResourceLocation BLACKBOARD_DYE_OUTLINE_SPRITE = Supplementaries.res("blackboard/dye_outline");
    public static final ResourceLocation BLACKBOARD_OUTLINE_SPRITE = Supplementaries.res("blackboard/outline");
    public static final ResourceLocation TRAPPED_PRESENT_BUTTON_SPRITE = Supplementaries.res("trapped_present/button");
    public static final ResourceLocation TRAPPED_PRESENT_BUTTON_HIGHLIGHTED_SPRITE = Supplementaries.res("trapped_present/button_highlighted");
    public static final ResourceLocation TRAPPED_PRESENT_BUTTON_SELECTED_SPRITE = Supplementaries.res("trapped_present/button_selected");
    public static final ResourceLocation TRAPPED_PRESENT_BUTTON_DISABLED_SPRITE = Supplementaries.res("trapped_present/button_disabled");
    public static final ResourceLocation PRESENT_BUTTON_SPRITE = Supplementaries.res("present/button");
    public static final ResourceLocation PRESENT_BUTTON_HIGHLIGHTED_SPRITE = Supplementaries.res("present/button_highlighted");
    public static final ResourceLocation PRESENT_BUTTON_SELECTED_SPRITE = Supplementaries.res("present/button_selected");
    public static final ResourceLocation PRESENT_BUTTON_DISABLED_SPRITE = Supplementaries.res("present/button_disabled");
    public static final ResourceLocation PRESENT_OVERLAY_SPRITE = Supplementaries.res("present/overlay");
    public static final ResourceLocation CANNON_POWER_SPRITE = Supplementaries.res("cannon/power");
    public static final ResourceLocation CANNON_POWER_HOVERED_SPRITE = Supplementaries.res("cannon/power_highlighted");
    public static final ResourceLocation CANNON_EMPTY_SPRITE = Supplementaries.res("cannon/empty");
    public static final ResourceLocation CANNON_EMPTY_HOVERED_SPRITE = Supplementaries.res("cannon/empty_highlighted");
    public static final ResourceLocation CANNON_DEPLETED_SPRITE = Supplementaries.res("cannon/depleted");
    public static final ResourceLocation CANNON_DEPLETED_HOVERED_SPRITE = Supplementaries.res("cannon/depleted_highlighted");
    public static final ResourceLocation CANNON_MANEUVER_SPRITE = Supplementaries.res("cannon/maneuver");
    public static final ResourceLocation CANNON_MANEUVER_HOVERED_SPRITE = Supplementaries.res("cannon/maneuver_highlighted");

    public static final ResourceLocation BOOK_ENCHANTED_TEXTURES = Supplementaries.res("block/books/book_enchanted");
    public static final ResourceLocation BOOK_TOME_TEXTURES = Supplementaries.res("block/books/book_tome");
    public static final ResourceLocation BOOK_WRITTEN_TEXTURES = Supplementaries.res("block/books/book_written");
    public static final ResourceLocation BOOK_AND_QUILL_TEXTURES = Supplementaries.res("block/books/book_and_quill");
    public static final ResourceLocation BOOK_ANTIQUE_TEXTURES = Supplementaries.res("block/books/book_antique");
    public static final ResourceLocation BUBBLE_BLOCK_TEXTURE = Supplementaries.res("block/bubble_block");
    public static final ResourceLocation BUBBLE_BLOCK_COLORS_TEXTURE = Supplementaries.res("block/bubble_block_colors");

    public static final ResourceLocation FLAG_ICON = Supplementaries.res("item/gui_slots/empty_slot_flag");
    public static final ResourceLocation BANNER_ICON = Supplementaries.res("item/gui_slots/empty_slot_banner");
    public static final ResourceLocation MAP_ICON = Supplementaries.res("item/gui_slots/empty_slot_map");
    public static final ResourceLocation ROPE_ICON = Supplementaries.res("item/gui_slots/empty_slot_rope");
    public static final ResourceLocation CHAIN_ICON = Supplementaries.res("item/gui_slots/empty_slot_chain");
    public static final ResourceLocation BANNER_PATTERN_ICON = Supplementaries.res("item/gui_slots/empty_slot_banner_pattern");
    public static final ResourceLocation BOOK_ICON = Supplementaries.res("item/gui_slots/empty_slot_book");
    public static final ResourceLocation ANTIQUE_INK_ICON = Supplementaries.res("item/gui_slots/empty_slot_antique_ink");
    public static final ResourceLocation MAP_ATLAS_ICON = Supplementaries.res("item/gui_slots/empty_slot_map_atlas");
    public static final ResourceLocation PAPER_ICON = Supplementaries.res("item/gui_slots/empty_slot_paper");
    public static final ResourceLocation GLASS_PANE_ICON = Supplementaries.res("item/gui_slots/empty_slot_glass_pane");


    public static final List<ResourceLocation> CARTOGRAPHY_INGREDIENTS_ICONS = Util.make(() -> {
        var l = new ArrayList<ResourceLocation>();
        l.add(PAPER_ICON);
        l.add(GLASS_PANE_ICON);
        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            l.add(ANTIQUE_INK_ICON);
        }
        if (CompatHandler.MAPATLAS) {
            l.add(MAP_ATLAS_ICON);
        }
        return ImmutableList.copyOf(l);
    });

    public static final List<ResourceLocation> MAP_ICONS = Util.make(() -> {
        var l = new ArrayList<ResourceLocation>();
        l.add(MAP_ICON);
        if (CompatHandler.MAPATLAS) {
            l.add(MAP_ATLAS_ICON);
        }
        return ImmutableList.copyOf(l);
    });

    public static final List<ResourceLocation> BANNER_SLOT_ICONS =
            List.of(BANNER_ICON, FLAG_ICON);

    public static final List<ResourceLocation> PULLEY_SLOT_ICONS =
            List.of(ROPE_ICON, CHAIN_ICON);

    public static final List<ResourceLocation> NOTICE_BOARD_SLOT_ICONS =
            List.of(MAP_ICON, BANNER_PATTERN_ICON, BOOK_ICON);

}