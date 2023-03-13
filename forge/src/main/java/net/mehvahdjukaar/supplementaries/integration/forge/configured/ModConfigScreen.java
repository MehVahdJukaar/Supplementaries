package net.mehvahdjukaar.supplementaries.integration.forge.configured;


import com.mrcrayfish.configured.api.IModConfig;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigScreen;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.configs.*;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//credits to MrCrayfish's Configured Mod
public class ModConfigScreen extends CustomConfigScreen {

    private static final Map<String, ItemStack> ICONS = new HashMap<>();

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
        addIcon("bells tweaks", Items.BELL);
        addIcon("cake tweaks", Items.CAKE);
        addIcon("dispenser tweaks", Items.DISPENSER);
        addIcon("hanging flower pots", Items.FLOWER_POT);
        addIcon("throwable bricks", Items.BRICK);
        addIcon("rope", ModRegistry.ROPE.get());
        addIcon("wall lantern", Items.LANTERN);
        addIcon("placeable sticks", Items.STICK);
        addIcon("placeable sticks", Items.STICK);
        addIcon("brewing stand colors", Items.BREWING_STAND);
        addIcon("timber frame", ModRegistry.TIMBER_BRACE_ITEM.get());
        addIcon("bottle xp", Items.EXPERIENCE_BOTTLE);
        addIcon("map tweaks", Items.FILLED_MAP);
        addIcon("ceiling banners", Items.RED_BANNER);
        addIcon("initialization", ModRegistry.COG_BLOCK.get());
        addIcon("zombie horse", Items.ZOMBIE_HORSE_SPAWN_EGG);
        addIcon("placeable gunpowder", Items.GUNPOWDER);
        addIcon("mixins", Items.HOPPER);
        addIcon("server protection", Items.COMMAND_BLOCK);
        addIcon("placeable books", Items.ENCHANTED_BOOK);
        addIcon("sign post", ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        addIcon("wattle and daub", ModRegistry.DAUB_BRACE.get());
        addIcon("shulker helmet", Items.SHULKER_SHELL);
        addIcon("jar tab", ModRegistry.JAR_ITEM.get());
        addIcon("dispensers", Items.DISPENSER);
        addIcon("hanging sign", ModRegistry.HANGING_SIGNS.get(WoodTypeRegistry.OAK_TYPE));
        addIcon("blue bomb", ModRegistry.BOMB_BLUE_ITEM_ON.get());
        addIcon("dispensers", Items.DISPENSER);
        addIcon("cave urns", ModRegistry.URN.get());
        addIcon("structures", Items.BRICKS);
        addIcon("soap", ModRegistry.SOAP.get());
        addIcon("mob head tweaks", Items.SKELETON_SKULL);
        addIcon("lantern tweaks", Items.LANTERN);
        addIcon("conditional sign registration", Items.BARRIER);
        addIcon("dispenser minecart", ModRegistry.DISPENSER_MINECART_ITEM.get());
        addIcon("traders open doors", Items.WANDERING_TRADER_SPAWN_EGG);
        addIcon("basalt ash", Items.BASALT);
        addIcon("cave urns", Items.BONE);
        addIcon("way sign", ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.getValue(new ResourceLocation("spruce"))));
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
    }


    public ModConfigScreen(CustomConfigSelectScreen parent, IModConfig config) {
        super(parent, config);
        this.icons.putAll(ICONS);
    }

    public ModConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                           Screen parent, IModConfig config) {
        super(modId, mainIcon, background, title, parent, config);
        this.icons.putAll(ICONS);
    }

    private static void addIcon(String s, ItemLike i) {
        ICONS.put(s, i.asItem().getDefaultInstance());
    }


    @Override
    public void onSave() {
        //sync stuff
        if (this.config.getFileName().contains("common")) {
            //TODO: fix. this work but shouldnt be needed and might break servers
            //TODO: configured should have something for this
            ConfigUtils.clientRequestServerConfigReload();
        }
    }

    @Override
    public CustomConfigScreen createSubScreen(Component title) {
        return new ModConfigScreen(this.modId, this.mainIcon, this.background, title, this, this.config);
    }


}