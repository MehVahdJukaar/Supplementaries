package net.mehvahdjukaar.supplementaries.compat.configured;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import joptsimple.internal.Strings;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

//credits to MrCrayfish's Configured Mod

public class ConfiguredCustomScreen extends ConfigScreen {

    private static final Map<String,ItemStack> ICONS = new HashMap<>();
    private final ItemStack MAIN_ICON = new ItemStack(Registry.GLOBE_ITEM.get());
    static{
        addIcon("blocks", Items.GRASS_BLOCK);
        addIcon("entities", Items.LEATHER);
        addIcon("general", Items.BOOK);
        addIcon("particles", Items.BLAZE_POWDER);
        addIcon("items", Items.LAPIS_LAZULI);
        addIcon("spawns", Items.TURTLE_EGG);
        addIcon("tweaks", Items.ENCHANTING_TABLE);
        addIcon("captured mobs", Items.CAVE_SPIDER_SPAWN_EGG);
        addIcon("clock block", Registry.CLOCK_BLOCK_ITEM.get());
        addIcon("firefly jar", Registry.FIREFLY_JAR_ITEM.get());
        addIcon("flag", Registry.FLAGS_ITEMS.get(DyeColor.WHITE).get());
        addIcon("globe", Registry.GLOBE_ITEM.get());
        addIcon("item shelf", Registry.ITEM_SHELF_ITEM.get());
        addIcon("pedestal", Registry.PEDESTAL_ITEM.get());
        addIcon("wind vane", Registry.WIND_VANE_ITEM.get());
        addIcon("pedestal", Registry.PEDESTAL_ITEM.get());
        addIcon("firefly", Registry.FIREFLY_SPAWN_EGG_ITEM.get());
        addIcon("firefly glow", Registry.FIREFLY_JAR_ITEM.get());
        addIcon("bellows", Registry.BELLOWS_ITEM.get());
        addIcon("blackboard", Registry.BLACKBOARD_ITEM.get());
        addIcon("cage", Registry.CAGE_ITEM.get());
        addIcon("candle holder", Registry.CANDLE_HOLDER_ITEM.get());
        addIcon("jar", Registry.EMPTY_JAR_ITEM.get());
        addIcon("notice board", Registry.NOTICE_BOARD_ITEM.get());
        addIcon("sack", Registry.SACK_ITEM.get());
        addIcon("safe", Registry.SAFE_ITEM.get());
        addIcon("speaker block", Registry.SPEAKER_BLOCK_ITEM.get());
        addIcon("spring launcher", Registry.PISTON_LAUNCHER_ITEM.get());
        addIcon("turn table", Registry.TURN_TABLE_ITEM.get());
        addIcon("flute", Registry.FLUTE_ITEM.get());
        addIcon("rope arrow", Registry.ROPE_ARROW_ITEM.get());
        addIcon("structures", Registry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
        addIcon("bells tweaks", Items.BELL);
        addIcon("cake tweaks", Items.CAKE);
        addIcon("hanging flower pots", Items.FLOWER_POT);
        addIcon("throwable bricks", Items.BRICK);
        addIcon("wall lantern", Items.LANTERN);
        addIcon("placeable sticks", Items.STICK);
        addIcon("brewing stand colors", Items.BREWING_STAND);
        addIcon("timber frame", Registry.TIMBER_BRACE_ITEM.get());
        addIcon("raked gravel", Registry.RAKED_GRAVEL_ITEM.get());
        addIcon("bottle xp", Items.EXPERIENCE_BOTTLE);
        addIcon("hourglass", Registry.HOURGLASS_ITEM.get());
        addIcon("map tweaks", Items.FILLED_MAP);
        addIcon("ceiling banners", Items.RED_BANNER);
        addIcon("initialization", Registry.COG_BLOCK_ITEM.get());
        addIcon("iron gate", Registry.IRON_GATE_ITEM.get());
        addIcon("zombie horse", Items.ROTTEN_FLESH);
        addIcon("bomb", Registry.BOMB_ITEM.get());
        addIcon("clock_right_click", Items.CLOCK);
    }

    public static void openScreen(){
        ServerConfigs.loadLocal();
        openScreen(Minecraft.getInstance());
    }
    private static void openScreen(Minecraft mc){
        mc.setScreen(new ConfiguredCustomScreen(mc.screen));
    }

    private static void addIcon(String s, Item i){
        ICONS.put(s,new ItemStack(i));
    }



    public static void registerScreen(){
        ModContainer container = ModList.get().getModContainerById(Supplementaries.MOD_ID).get();
        container.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, screen) -> new ConfiguredCustomScreen(screen));

    }

    private ConfigScreen.ConfigList list = null;

    //needed for custom title
    public ConfiguredCustomScreen(Screen parent) {
        super(parent, "\u00A76Supplementaries Configured",
                Collections.singletonList(new ConfigFileEntry(ClientConfigs.CLIENT_CONFIG, ClientConfigs.CLIENT_CONFIG.getValues())),
                Arrays.asList(new ConfigFileEntry(ServerConfigs.SERVER_CONFIG, ServerConfigs.SERVER_CONFIG.getValues()),
                        new ConfigFileEntry(RegistryConfigs.REGISTRY_CONFIG, RegistryConfigs.REGISTRY_CONFIG.getValues())),
                Textures.EMPTY_TEXTURE);
    }

    public ConfiguredCustomScreen(Screen parent, String displayName, ForgeConfigSpec spec, UnmodifiableConfig values) {
        super(parent,displayName,new ConfigFileEntry(spec,values),Textures.EMPTY_TEXTURE);
    }



    //this is the worst thing ever. Idk why I did this
    @Override
    protected void init() {
        super.init();
        try {
            Field f = ObfuscationReflectionHelper.findField(ConfigScreen.class,"list");
            f.setAccessible(true);
            this.list = (ConfigScreen.ConfigList)f.get(this);

        }catch (Exception ignored){}

        Field f;
        try {
            f = ObfuscationReflectionHelper.findField(ConfigScreen.SubMenu.class,"button");
            f.setAccessible(true);
        }catch (Exception e){
            return;
        }
        List<?> children = list.children();
        boolean isCommon = false;

        for(Object c : children){
            if(c instanceof SubMenu){
                SubMenu subMenu = (SubMenu) c;

                if(!isCommon) {
                    modifySubmenus(f, subMenu, ClientConfigs.CLIENT_CONFIG);
                }
                else{
                    modifySubmenus(f, subMenu, ServerConfigs.SERVER_CONFIG);
                    //TODO: add icons to reg configs
                    //modifySubmenus(f, subMenu, RegistryConfigs.REGISTRY_CONFIG);

                }
                if(subMenu.getLabel().equals("Tweaks"))isCommon = true;
            }
        }

    }

    private void modifySubmenus(Field f, SubMenu subMenu, ForgeConfigSpec spec){

        spec.getValues().valueMap().forEach((s, o) -> {
            if (o instanceof AbstractConfig) {
                String label = createLabel(s);
                if (subMenu.getLabel().equals(label)) {
                    f.setAccessible(true);
                    try {
                        f.set(subMenu, new Button(10, 5, 44, 20,
                                (new StringTextComponent(label)).withStyle(TextFormatting.BOLD).withStyle(TextFormatting.WHITE),
                                (onPress) -> {
                                    String newTitle = "\u00A76Supplementaries" + " > " + label;
                                    this.minecraft.setScreen(new ConfiguredCustomScreen(this, newTitle, spec, (UnmodifiableConfig) o));
                                }));
                    } catch (IllegalAccessException ignored) {}
                }
            }
        });
    }


    private static String createLabel(String input) {
        String[] words = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

        int i;
        for(i = 0; i < words.length; ++i) {
            words[i] = StringUtils.capitalize(words[i]);
        }

        String valueName = Strings.join(words, " ");
        words = valueName.split("_");

        for(i = 0; i < words.length; ++i) {
            words[i] = StringUtils.capitalize(words[i]);
        }

        return Strings.join(words, " ");
    }


    //filthy hax
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (ScreenUtil.isMouseWithin((this.width/2)-90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, this.minecraft.font.split(new TranslationTextComponent("supplementaries.gui.info"), 200), mouseX, mouseY);
        }

        //drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 7, 16777215);
        Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(MAIN_ICON, (this.width/2)+90-17, 2);
        Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(MAIN_ICON, (this.width/2)-90, 2);
        if(this.list!=null){
            double scrollAmount;
            try {
                Field f = ObfuscationReflectionHelper.findField(AbstractList.class,"field_230678_o_");
                f.setAccessible(true);
                scrollAmount = (double) f.get(this.list);

            }catch (Exception e){
                return;
            }

            List<?> children = list.children();

            int y0 = 50;
            int y1 = this.height - 36;
            int itemHeight = 24;
            int headerHeight = 0;

            int size = children.size();

            for(int index = 0; index < size; ++index) {
                int top = y0 + 6 - (int)scrollAmount + index * itemHeight + headerHeight;

                if (top >= y0 && top+itemHeight <= y1+8) {

                    if (children.get(index) instanceof SubMenu) {
                        SubMenu button = (SubMenu) children.get(index);


                        ItemStack icon = ICONS.getOrDefault(button.getLabel().toLowerCase(),MAIN_ICON);
                        int center = this.list.getRowLeft() + this.list.getRowWidth()/2;

                        Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(icon, center+90-17, top);
                        Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(icon, center-90, top);
                    }
                }
            }


        }

    }


    @Override
    public void renderBackground(MatrixStack p_230446_1_) {
    }

    public void renderBackground(MatrixStack p_238651_1_, int p_238651_2_) {
    }

    @Override
    public void renderDirtBackground(int vOffset) {}

    @Override
    public void removed() {
        super.removed();

        //TODO: only sync cached stuff
        ClientConfigs.cached.refresh();
        ServerConfigs.cached.refresh();

        //reload server values and get new ones with packet
        //this isn't working...
        //NetworkHandler.INSTANCE.sendToServer(new RequestConfigReloadPacket());

    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ScreenUtil.isMouseWithin((this.width/2)-90, 2, 180, 16, (int)mouseX, (int)mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/supplementaries"));
            this.handleComponentClicked(style);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
