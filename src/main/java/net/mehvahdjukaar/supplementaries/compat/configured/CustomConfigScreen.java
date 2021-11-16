package net.mehvahdjukaar.supplementaries.compat.configured;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//credits to MrCrayfish's Configured Mod
public class CustomConfigScreen extends ConfigScreen {

    private final ResourceLocation background;

    private static final Map<String, ItemStack> ICONS = new HashMap<>();
    private static final ItemStack MAIN_ICON = new ItemStack(ModRegistry.GLOBE_ITEM.get());

    static {
        addIcon("blocks", Items.GRASS_BLOCK);
        addIcon("entities", Items.LEATHER);
        addIcon("general", Items.BOOK);
        addIcon("particles", Items.BLAZE_POWDER);
        addIcon("items", Items.LAPIS_LAZULI);
        addIcon("spawns", Items.TURTLE_EGG);
        addIcon("tweaks", Items.ENCHANTING_TABLE);
        addIcon("captured mobs", Items.CAVE_SPIDER_SPAWN_EGG);
        addIcon("clock block", ModRegistry.CLOCK_BLOCK_ITEM.get());
        addIcon("firefly jar", ModRegistry.FIREFLY_JAR_ITEM.get());
        addIcon("flag", ModRegistry.FLAGS_ITEMS.get(DyeColor.WHITE).get());
        addIcon("globe", ModRegistry.GLOBE_ITEM.get());
        addIcon("item shelf", ModRegistry.ITEM_SHELF_ITEM.get());
        addIcon("pedestal", ModRegistry.PEDESTAL_ITEM.get());
        addIcon("wind vane", ModRegistry.WIND_VANE_ITEM.get());
        addIcon("pedestal", ModRegistry.PEDESTAL_ITEM.get());
        addIcon("firefly", ModRegistry.FIREFLY_SPAWN_EGG_ITEM.get());
        addIcon("firefly glow", ModRegistry.FIREFLY_JAR_ITEM.get());
        addIcon("bellows", ModRegistry.BELLOWS_ITEM.get());
        addIcon("blackboard", ModRegistry.BLACKBOARD_ITEM.get());
        addIcon("cage", ModRegistry.CAGE_ITEM.get());
        addIcon("candle holder", ModRegistry.CANDLE_HOLDER_ITEM.get());
        addIcon("jar", ModRegistry.JAR_ITEM.get());
        addIcon("notice board", ModRegistry.NOTICE_BOARD_ITEM.get());
        addIcon("sack", ModRegistry.SACK_ITEM.get());
        addIcon("safe", ModRegistry.SAFE_ITEM.get());
        addIcon("speaker block", ModRegistry.SPEAKER_BLOCK_ITEM.get());
        addIcon("spring launcher", ModRegistry.PISTON_LAUNCHER_ITEM.get());
        addIcon("turn table", ModRegistry.TURN_TABLE_ITEM.get());
        addIcon("flute", ModRegistry.FLUTE_ITEM.get());
        addIcon("rope arrow", ModRegistry.ROPE_ARROW_ITEM.get());
        addIcon("structures", ModRegistry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
        addIcon("bells tweaks", Items.BELL);
        addIcon("cake tweaks", Items.CAKE);
        addIcon("hanging flower pots", Items.FLOWER_POT);
        addIcon("throwable bricks", Items.BRICK);
        addIcon("wall lantern", Items.LANTERN);
        addIcon("placeable sticks", Items.STICK);
        addIcon("brewing stand colors", Items.BREWING_STAND);
        addIcon("timber frame", ModRegistry.TIMBER_BRACE_ITEM.get());
        addIcon("raked gravel", ModRegistry.RAKED_GRAVEL_ITEM.get());
        addIcon("bottle xp", Items.EXPERIENCE_BOTTLE);
        addIcon("hourglass", ModRegistry.HOURGLASS_ITEM.get());
        addIcon("map tweaks", Items.FILLED_MAP);
        addIcon("ceiling banners", Items.RED_BANNER);
        addIcon("initialization", ModRegistry.COG_BLOCK_ITEM.get());
        addIcon("iron gate", ModRegistry.IRON_GATE_ITEM.get());
        addIcon("zombie horse", Items.ROTTEN_FLESH);
        addIcon("bomb", ModRegistry.BOMB_ITEM.get());
        addIcon("blue bomb", ModRegistry.BOMB_BLUE_ITEM.get());
        addIcon("placeable gunpowder", Items.GUNPOWDER);
        addIcon("mixins", Items.HOPPER);
        addIcon("slingshot", ModRegistry.SLINGSHOT_ITEM.get());
        addIcon("server protection", Items.COMMAND_BLOCK);
        addIcon("bamboo spikes", ModRegistry.BAMBOO_SPIKES_ITEM.get());
        addIcon("placeable books", Items.ENCHANTED_BOOK);

        Field temp = null;
        try {
            temp = ObfuscationReflectionHelper.findField(FolderEntry.class, "label");
        } catch (Exception ignored) {
        } finally {
            FOLDER_LABEL = temp;
        }
        Field temp1 = null;
        try {
            temp1 = ObfuscationReflectionHelper.findField(ConfigScreen.class, "folderEntry");
        } catch (Exception ignored) {
        } finally {
            FOLDER_ENTRY = temp1;
        }
        Method temp2 = null;
        try {
            temp2 = ObfuscationReflectionHelper.findMethod(ConfigScreen.class, "saveConfig");
        } catch (Exception ignored) {
        } finally {
            SAVE_CONFIG = temp2;
        }
        Field temp3 = null;
        try {
            temp3 = ObfuscationReflectionHelper.findField(Button.class, "onPress");
        } catch (Exception ignored) {
        } finally {
            BUTTON_ON_PRESS = temp3;
        }
    }

    private static void addIcon(String s, net.minecraft.item.Item i) {
        ICONS.put(s, new ItemStack(i));
    }

    @Nullable
    private static final Field FOLDER_LABEL;
    @Nullable
    private static final Field BUTTON_ON_PRESS;
    @Nullable
    private static final Field FOLDER_ENTRY;
    @Nullable
    private static final Method SAVE_CONFIG;

    private CustomConfigScreen(Screen parent, ITextComponent title, ResourceLocation background, ConfigScreen.FolderEntry folderEntry) {
        this(parent, title, ConfigHandler.CLIENT_CONFIG_OBJECT, background);
        //hax
        try {
            FOLDER_ENTRY.set(this, folderEntry);
        } catch (Exception ignored) {
        }
    }

    //needed for custom title
    public CustomConfigScreen(Screen parent, ITextComponent title, ModConfig config, ResourceLocation background) {
        super(parent, title, config, background);
        this.background = background;
    }

    @Override
    protected void init() {
        super.init();

        EntryList newList = new EntryList(Collections.emptyList());
        //replace list with new custom entries
        for (Item c : list.children()) {
            if (c instanceof FolderItem) {
                FolderWrapper wrapper = wrapFolderItem((FolderItem) c);
                if (wrapper != null) {
                    newList.children().add(wrapper);
                    continue;
                }
            }
            newList.children().add(c);
        }
        this.list.replaceEntries(newList.children());

        //overrides save button
        if (this.saveButton != null && SAVE_CONFIG != null && BUTTON_ON_PRESS != null) {
            try {
                Button.IPressable press = this::saveButtonAction;
                BUTTON_ON_PRESS.set(this.saveButton, press);
            } catch (Exception ignored) {
            }
            ;
        }
    }

    //sync configs to server when saving
    public void saveButtonAction(Button button) {
        if (this.config != null) {
            try {
                SAVE_CONFIG.invoke(this);
            } catch (Exception ignored) {
            }

            if (this.isChanged(this.folderEntry)) {
                if (this.config == ConfigHandler.SERVER_CONFIG_OBJECT) {
                    ConfigHandler.clientRequestServerConfigReload();
                } else if (this.config == ConfigHandler.CLIENT_CONFIG_OBJECT) {
                    ClientConfigs.cached.refresh();
                }
            }
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, this.font.split(new TranslationTextComponent("supplementaries.gui.info"), 200), mouseX, mouseY);
        }
        int titleWidth = this.font.width(this.title) + 35;
        this.itemRenderer.renderAndDecorateFakeItem(MAIN_ICON, (this.width / 2) + titleWidth / 2 - 17, 2);
        this.itemRenderer.renderAndDecorateFakeItem(MAIN_ICON, (this.width / 2) - titleWidth / 2, 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/supplementaries"));
            this.handleComponentClicked(style);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private int ticks = 0;

    @Override
    public void tick() {
        super.tick();
        ticks++;
    }

    @Nullable
    public FolderWrapper wrapFolderItem(FolderItem old) {
        final FolderEntry folderEntry = CustomConfigScreen.this.folderEntry;

        try {
            String oldName = old.getLabel();
            //find correct folder
            FolderEntry found = null;
            for (IEntry e : folderEntry.getEntries()) {
                if (e instanceof FolderEntry) {
                    String n = new StringTextComponent(ConfigScreen.createLabel((String) FOLDER_LABEL.get(e))).getContents();
                    if (n.equals(oldName)) found = (FolderEntry) e;
                }
            }
            if (found != null) {
                return new FolderWrapper(found, oldName);
            }
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    private class FolderWrapper extends FolderItem {

        private final ItemStack icon;
        protected final Button button;

        private FolderWrapper(FolderEntry folderEntry, String label) {
            super(folderEntry);
            //make new button I can access
            this.button = new Button(10, 5, 44, 20, (new StringTextComponent(label)).withStyle(TextFormatting.BOLD).withStyle(TextFormatting.WHITE), (onPress) -> {
                ITextComponent newTitle = CustomConfigScreen.this.title.plainCopy().append(" > " + label);
                CustomConfigScreen.this.minecraft.setScreen(new CustomConfigScreen(CustomConfigScreen.this,
                        newTitle, CustomConfigScreen.this.background, folderEntry));
            });
            this.icon = ICONS.getOrDefault(label.toLowerCase(), MAIN_ICON);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of(this.button);
        }

        private int ticks = 0;
        private int lastTick = 1;

        @Override
        public void render(MatrixStack matrixStack, int x, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean selected, float partialTicks) {

            if (selected) {
                if (lastTick < CustomConfigScreen.this.ticks) ticks++;
                this.lastTick = CustomConfigScreen.this.ticks;
            } else {
                ticks = 0;
            }


            this.button.x = left - 1;
            this.button.y = top;
            this.button.setWidth(width);
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            int center = this.button.x + width / 2;

            ItemRenderer renderer = CustomConfigScreen.this.itemRenderer;

            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            RenderSystem.translatef(center + 90 - 17, top + 2, -100.0F - renderer.blitOffset - 100);
            RenderSystem.translatef(8.0F, 8.0F, 0.0F);
            if (selected) {
                float scale = 1 + 0.1f * MathHelper.sin((float) (((ticks + partialTicks) / 3f) % (2 * Math.PI)));
                RenderSystem.scalef(scale, scale, scale);
                //RenderSystem.rotatef(((ticks + partialTicks) /10f));
            }
            RenderSystem.translatef(-8.0F, -8.0F, 0.0F);
            RenderSystem.translatef(0, 0, +100.0F + renderer.blitOffset);
            renderer.renderAndDecorateFakeItem(icon, 0, 0);
            RenderSystem.disableRescaleNormal();
            RenderSystem.popMatrix();

            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            RenderSystem.translatef(center - 90, top + 2, -100.0F - renderer.blitOffset + 100);
            RenderSystem.translatef(8.0F, 8.0F, 0.0F);
            if (selected) {
                float scale = 1 + 0.1f * MathHelper.sin((float) (((ticks + partialTicks) / 3f) % (2 * Math.PI)));
                RenderSystem.scalef(scale, scale, scale);
                //RenderSystem.rotatef(((ticks + partialTicks) /10f));
            }
            RenderSystem.translatef(-8.0F, -8.0F, 0.0F);
            RenderSystem.translatef(0, 0, +100.0F + renderer.blitOffset);
            renderer.renderAndDecorateFakeItem(icon, 0, 0);
            RenderSystem.disableRescaleNormal();
            RenderSystem.popMatrix();

        }
    }
}
