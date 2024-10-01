package net.mehvahdjukaar.supplementaries.integration.neoforge.configured;


import com.mrcrayfish.configured.api.IModConfig;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigScreen;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

//credits to MrCrayfish's Configured Mod
public class ModConfigScreen extends CustomConfigScreen {

    private static final Map<String, ItemStack> CUSTOM_ICONS = new HashMap<>();

    static {
        addIcon("blocks", Items.OXIDIZED_COPPER);
        addIcon("entities", Items.RABBIT_HIDE);
        addIcon("general", Items.BOOKSHELF);
        addIcon("particles", Items.BLAZE_POWDER);
        addIcon("items", Items.AMETHYST_SHARD);
        addIcon("spawns", Items.TURTLE_EGG);
        addIcon("tweaks", ModRegistry.WRENCH.get());
        addIcon("turn particles", ModRegistry.TURN_TABLE.get());
        addIcon("captured mobs", ModRegistry.CAGE_ITEM.get());
        addIcon("flag", ModRegistry.FLAGS.get(DyeColor.WHITE).get());
        addIcon("way sign", ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        addIcon("dispenser tweaks", Items.DISPENSER);
        addIcon("golden apple disenchant", Items.ENCHANTED_GOLDEN_APPLE);
        addIcon("throwable bricks", Items.BRICK);
        addIcon("throwable bricks", Items.BRICK);
        addIcon("rope", ModRegistry.ROPE.get());
        addIcon("placeable sticks", Items.STICK);
        addIcon("item lore", Items.NAME_TAG);
        addIcon("timber frame", ModRegistry.TIMBER_BRACE_ITEM.get());
        addIcon("bottle xp", Items.EXPERIENCE_BOTTLE);
        addIcon("sus recipes", Items.SUSPICIOUS_SAND);
        addIcon("slimed effect", Items.SLIME_BALL);
        addIcon("map tweaks", Items.FILLED_MAP);
        addIcon("initialization", ModRegistry.COG_BLOCK.get());
        addIcon("zombie horse", Items.ZOMBIE_HORSE_SPAWN_EGG);
        addIcon("placeable gunpowder", Items.GUNPOWDER);
        addIcon("noteblocks scare", Items.NOTE_BLOCK);
        addIcon("mixins", Items.HOPPER);
        addIcon("lumisene", ModFluids.LUMISENE_BUCKET.get());
        addIcon("server protection", Items.COMMAND_BLOCK);
        addIcon("placeable books", Items.ENCHANTED_BOOK);
        addIcon("sign post", ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        addIcon("wattle and daub", ModRegistry.DAUB_BRACE.get());
        addIcon("shulker helmet", Items.SHULKER_SHELL);
        addIcon("jar tab", ModRegistry.JAR_ITEM.get());
        addIcon("dispensers", Items.DISPENSER);
        addIcon("blue bomb", ModRegistry.BOMB_BLUE_ITEM.get());
        addIcon("dispensers", Items.DISPENSER);
        addIcon("cave urns", ModRegistry.URN.get());
        addIcon("structures", Items.BRICKS);
        addIcon("soap", ModRegistry.SOAP.get());
        addIcon("awning", ModRegistry.AWNINGS.get(DyeColor.RED).get());
        addIcon("mob head tweaks", Items.SKELETON_SKULL);
        addIcon("lantern tweaks", Items.LANTERN);
        addIcon("conditional sign registration", Items.BARRIER);
        addIcon("dispenser minecart", ModRegistry.DISPENSER_MINECART_ITEM.get());
        addIcon("traders open doors", Items.WANDERING_TRADER_SPAWN_EGG);
        addIcon("basalt ash", Items.BASALT);
        addIcon("cave urns", Items.BONE);
        addIcon("way sign", ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.getValue(ResourceLocation.withDefaultNamespace("spruce"))));
        addIcon("stasis", Items.ENCHANTED_BOOK);
        addIcon("banner pattern tooltip", Items.CREEPER_BANNER_PATTERN);
        addIcon("paintings tooltip", Items.PAINTING);
        addIcon("clock right click", Items.CLOCK);
        addIcon("compass right click", Items.COMPASS);
        addIcon("crossbows colors", Items.CROSSBOW);
        addIcon("mob head shaders", Items.DRAGON_HEAD);
        addIcon("placeable books glint", Items.BOOK);

        addIcon("redstone", Items.REDSTONE);
        addIcon("building", Items.OXIDIZED_COPPER);
        addIcon("utilities", Items.BARREL);
        addIcon("functional", Items.AMETHYST_SHARD);
        addIcon("tools", Items.GOLDEN_PICKAXE);
        addIcon("enhanced hanging signs", Items.OAK_HANGING_SIGN);
    }


    public ModConfigScreen(CustomConfigSelectScreen parent, IModConfig config) {
        super(parent, config);
        this.icons.putAll(CUSTOM_ICONS);
    }

    public ModConfigScreen(String modId, ItemStack mainIcon, Component title,
                           Screen parent, IModConfig config) {
        super(modId, mainIcon, title, parent, config);
        this.icons.putAll(CUSTOM_ICONS);
    }

    private static void addIcon(String s, ItemLike i) {
        CUSTOM_ICONS.put(s, i.asItem().getDefaultInstance());
    }


    @Override
    public void onSave() {
        //sync stuff
        if (this.config.getFileName().contains("common")) {
            //TODO: fix. this work but shouldnt be needed and might break servers
            //TODO: configured should have something for this
          //  ConfigUtils.clientRequestServerConfigReload();
        }
    }

    @Override
    public Factory getSubScreenFactory() {
        return ModConfigScreen::new;
    }

}