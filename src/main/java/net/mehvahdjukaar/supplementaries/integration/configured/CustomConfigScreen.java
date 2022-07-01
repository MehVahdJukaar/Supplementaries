package net.mehvahdjukaar.supplementaries.integration.configured;


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import net.mehvahdjukaar.moonlight.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

//credits to MrCrayfish's Configured Mod
public class CustomConfigScreen extends ConfigScreen {

    private final ResourceLocation background;

    private static final Map<String, ItemStack> ICONS = new HashMap<>();
    public static final ItemStack MAIN_ICON = new ItemStack(ModRegistry.GLOBE_ITEM.get());

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
        addIcon("flag", ModRegistry.FLAGS_ITEMS.get(DyeColor.WHITE).get());
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
        addIcon("initialization", ModRegistry.COG_BLOCK_ITEM.get());
        addIcon("zombie horse", Items.ZOMBIE_HORSE_SPAWN_EGG);
        addIcon("placeable gunpowder", Items.GUNPOWDER);
        addIcon("mixins", Items.HOPPER);
        addIcon("server protection", Items.COMMAND_BLOCK);
        addIcon("placeable books", Items.ENCHANTED_BOOK);
        addIcon("sign post", ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        addIcon("wattle and daub", ModRegistry.DAUB_BRACE_ITEM.get());
        addIcon("shulker shell", Items.SHULKER_SHELL);
        addIcon("jar tab", ModRegistry.JAR_ITEM.get());
        addIcon("custom configured screen", ModRegistry.WRENCH.get());
        addIcon("dispensers", Items.DISPENSER);
        addIcon("hanging sign", ModRegistry.HANGING_SIGNS_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        addIcon("blue bomb", ModRegistry.BOMB_BLUE_ITEM_ON.get());
        addIcon("dispensers", Items.DISPENSER);
        addIcon("cave urns", ModRegistry.URN_ITEM.get());
        addIcon("structures", Items.BRICKS);
        addIcon("soap", ModRegistry.SOAP_BLOCK.get());
        addIcon("mob head tweaks", Items.SKELETON_SKULL);
        addIcon("lantern tweaks", Items.LANTERN);
        addIcon("conditional sign registration", Items.BARRIER);
        addIcon("dispenser minecart", ModRegistry.DISPENSER_MINECART_ITEM.get());

    }

    private ItemStack getIcon(String name) {
        if (!ICONS.containsKey(name)) {
            String formatted = name.toLowerCase(Locale.ROOT).replace(" ", "_");
            var item = ForgeRegistries.ITEMS.getValue(Supplementaries.res(formatted));
            if (item != Items.AIR) {
                addIcon(name, item);
            }
        }
        return ICONS.getOrDefault(name, MAIN_ICON);

    }

    @Nullable
    private static Method findMethodOrNull(Class<?> c, String methodName) {
        Method field = null;
        try {
            field = ObfuscationReflectionHelper.findMethod(c, methodName);
        } catch (Exception ignored) {
        }
        return field;
    }

    @Nullable
    private static Field findFieldOrNull(Class<?> c, String fieldName) {
        Field field = null;
        try {
            field = ObfuscationReflectionHelper.findField(c, fieldName);
        } catch (Exception ignored) {
        }
        return field;
    }

    private static void addIcon(String s, ItemLike i) {
        ICONS.put(s, i.asItem().getDefaultInstance());
    }

    @Nullable
    private static final Field FOLDER_LABEL = findFieldOrNull(FolderEntry.class, "label");
    @Nullable
    private static final Field BUTTON_ON_PRESS = findFieldOrNull(Button.class, "onPress");
    @Nullable
    private static final Field FOLDER_ENTRY = findFieldOrNull(ConfigScreen.class, "folderEntry");
    @Nullable
    private static final Method SAVE_CONFIG = findMethodOrNull(ConfigScreen.class, "saveConfig");
    @Nullable
    private static final Field CONFIG_VALUE_HOLDER = findFieldOrNull(ConfigItem.class, "holder");
    @Nullable
    private static final Field BOOLEAN_ITEM_BUTTON = findFieldOrNull(BooleanItem.class, "button");

    private CustomConfigScreen(Screen parent, Component title, ModConfig config, ResourceLocation background, ConfigScreen.FolderEntry folderEntry) {
        this(parent, title, config, background);
        //hax
        try {
            FOLDER_ENTRY.set(this, folderEntry);
        } catch (Exception ignored) {
        }
    }

    //needed for custom title
    public CustomConfigScreen(Screen parent, Component title, ModConfig config, ResourceLocation background) {
        super(parent, title, config, background);
        this.background = background;
    }

    @Override
    protected void init() {
        super.init();

        //replace list with new custom entries
        boolean reg = this.config == ConfigHandler.REGISTRY_CONFIGS && !this.folderEntry.isRoot();

        this.list.replaceEntries(replaceItems(this.list.children(), reg));
        Collection<Item> temp = replaceItems(this.entries, reg);
        this.entries = new ArrayList<>(temp);

        //overrides save button
        if (this.saveButton != null && SAVE_CONFIG != null && BUTTON_ON_PRESS != null) {
            try {
                Button.OnPress press = this::saveButtonAction;
                BUTTON_ON_PRESS.set(this.saveButton, press);
            } catch (Exception ignored) {
            }
        }
    }

    private Collection<Item> replaceItems(Collection<Item> originals, boolean fancyBooleans) {
        ArrayList<Item> newList = new ArrayList<>();
        for (Item c : originals) {
            if (c instanceof FolderItem f) {
                FolderWrapper wrapper = wrapFolderItem(f);
                if (wrapper != null) {
                    newList.add(wrapper);
                    continue;
                }
            } else if (c instanceof BooleanItem b) {
                BooleanWrapper wrapper = wrapBooleanItem(b, fancyBooleans);
                if (wrapper != null) {
                    newList.add(wrapper);
                    continue;
                }
            }
            newList.add(c);
        }
        return newList;
    }

    //sync configs to server when saving
    public void saveButtonAction(Button button) {
        if (this.config != null) {
            try {
                SAVE_CONFIG.invoke(this);
            } catch (Exception ignored) {
            }

            if (this.isChanged(this.folderEntry)) {
                if (this.config == ConfigHandler.SERVER_CONFIGS) {
                    //TODO: fix. this work but shouldnt be needed and might break servers
                    ConfigHandler.clientRequestServerConfigReload();
                } else if (this.config == ConfigHandler.CLIENT_CONFIGS) {
                    ClientConfigs.cached.refresh();
                }
            }
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, this.font.split(Component.translatable("supplementaries.gui.info"), 200), mouseX, mouseY);
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
                if (e instanceof FolderEntry f) {
                    String n = Component.literal(ConfigScreen.createLabel((String) FOLDER_LABEL.get(e))).getString();
                    if (n.equals(oldName)) found = f;
                }
            }
            if (found != null) {
                return new FolderWrapper(found, oldName);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private class FolderWrapper extends FolderItem {

        private final ItemStack icon;
        protected final Button button;

        private FolderWrapper(FolderEntry folderEntry, String label) {
            super(folderEntry);
            //make new button I can access
            this.button = new Button(10, 5, 44, 20, (Component.literal(label)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), (onPress) -> {
                Component newTitle = CustomConfigScreen.this.title.plainCopy().append(" > " + label);
                CustomConfigScreen.this.minecraft.setScreen(new CustomConfigScreen(CustomConfigScreen.this,
                        newTitle, CustomConfigScreen.this.config, CustomConfigScreen.this.background, folderEntry));
            });
            this.icon = getIcon(label.toLowerCase(Locale.ROOT));
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.button);
        }

        private int ticks = 0;
        private int lastTick = 1;

        @Override
        public void render(PoseStack matrixStack, int x, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean selected, float partialTicks) {


            if (lastTick < CustomConfigScreen.this.ticks) {
                ticks = Math.max(0, ticks + (selected ? 1 : -2)) % (36);
            }

            this.lastTick = CustomConfigScreen.this.ticks;


            this.button.x = left - 1;
            this.button.y = top;
            this.button.setWidth(width);
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            int center = this.button.x + width / 2;

            ItemRenderer renderer = CustomConfigScreen.this.itemRenderer;

            float p = (float) (Math.PI / 180f);

            RendererUtil.renderGuiItemRelative(this.icon, center + 90 - 17, top + 2, renderer, (s, r) -> {
                if (ticks != 0) {
                    if (r) {
                        s.mulPose(Vector3f.YP.rotation(((ticks + partialTicks) * p * 10f)));

                    } else {
                        float scale = 1 + 0.1f * Mth.sin(((ticks + partialTicks) * p * 20));
                        s.scale(scale, scale, scale);
                    }
                }
            });

            RendererUtil.renderGuiItemRelative(this.icon, center - 90, top + 2, renderer, (s, r) -> {
                if (ticks != 0) {
                    if (r) {
                        s.mulPose(Vector3f.YP.rotation((ticks + partialTicks) * p * 10f));

                    } else {
                        float scale = 1 + 0.1f * Mth.sin((ticks + partialTicks) * p * 20);
                        s.scale(scale, scale, scale);
                    }
                }
            });


        }

    }

    @Nullable
    public BooleanWrapper wrapBooleanItem(BooleanItem old, boolean displayItem) {
        final FolderEntry folderEntry = CustomConfigScreen.this.folderEntry;
        try {
            ValueHolder<Boolean> holder = (ValueHolder<Boolean>) CONFIG_VALUE_HOLDER.get(old);

            //find correct folder
            ValueEntry found = null;
            for (IEntry e : folderEntry.getEntries()) {
                if (e instanceof ConfigScreen.ValueEntry value) {
                    if (holder == value.getHolder()) found = value;
                }
            }
            if (found != null) {
                return displayItem ? new BooleanWrapperItem(holder) : new BooleanWrapper(holder);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private class BooleanWrapperItem extends BooleanWrapper {

        private final ItemStack item;

        public BooleanWrapperItem(ValueHolder<Boolean> holder) {
            super(holder);

            this.item = getIcon(label.getString().toLowerCase(Locale.ROOT));
            this.iconOffset = 7;
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            boolean on = this.holder.getValue();
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);

            int light = LightTexture.FULL_BRIGHT;
            if (!on) {

                //int sky = LightTexture.sky(light);
                //int block = 0;//LightTexture.block(light);
                light = 0;//LightTexture.pack(block, sky);
            }
            int center = (int) (this.button.x + this.button.getWidth() / 2f);
            ItemRenderer renderer = CustomConfigScreen.this.itemRenderer;

            RendererUtil.renderGuiItemRelative(this.item, center - 8 - iconOffset, top + 2, renderer, (a, b) -> {
            }, light, OverlayTexture.NO_OVERLAY);
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(Component.literal(""));
        }
    }

    private class BooleanWrapper extends BooleanItem {
        private static final int ICON_WIDTH = 12;
        protected Button button;
        protected boolean active = false;
        protected int iconOffset = 0;

        public BooleanWrapper(ValueHolder<Boolean> holder) {
            super(holder);
            try {
                button = (Button) BOOLEAN_ITEM_BUTTON.get(this);
            } catch (Exception ignored) {
            }
            button.setMessage(Component.literal(""));
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            this.button.setMessage(Component.literal(""));
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, CustomConfigSelectScreen.ICONS_TEXTURES);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            int iconX = iconOffset + (int) (this.button.x + Math.ceil((this.button.getWidth() - ICON_WIDTH) / 2f));
            int iconY = (int) (this.button.y + Math.ceil(((this.button.getHeight() - ICON_WIDTH) / 2f)));

            int u = this.holder.getValue() ? ICON_WIDTH : 0;

            blit(poseStack, iconX, iconY, this.button.getBlitOffset(), (float) u, (float) 0, ICON_WIDTH, ICON_WIDTH, 64, 64);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(Component.literal(""));
        }
    }


}






    /*
    public class StringColorWrapper extends ConfigScreen.ConfigItem<String> {
        private final FocusedEditBox textField;

        public StringColorWrapper(ConfigScreen.ValueHolder<String> holder) {
            super(holder);
            this.textField = new FocusedEditBox(CustomConfigScreen.this.font, 0, 0, 44, 18, this.label);
            this.textField.setValue((holder.getValue()));
            this.textField.setResponder((s) -> {
                try {
                    if (holder.valueSpec.spawnParticleOnBoundingBox(s)) {
                        this.textField.setTextColor(14737632);
                        holder.setValue(s);
                        CustomConfigScreen.this.updateButtons();
                    } else {
                        this.textField.setTextColor(16711680);
                    }
                } catch (Exception var5) {
                    this.textField.setTextColor(16711680);
                }

            });
            this.eventListeners.add(this.textField);
        }

        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.textField.x = left + width - 68;
            this.textField.y = top + 1;
            this.textField.render(poseStack, mouseX, mouseY, partialTicks);
        }

        public void onResetValue() {
            this.textField.setValue((this.holder.getValue()));
        }
    }
    */
