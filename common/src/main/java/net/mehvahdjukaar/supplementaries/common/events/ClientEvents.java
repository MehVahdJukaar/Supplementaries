package net.mehvahdjukaar.supplementaries.common.events;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.client.screens.ConfigButton;
import net.mehvahdjukaar.supplementaries.client.screens.WelcomeMessageScreen;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeBlock;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.network.SyncEquippedQuiverPacket;
import net.mehvahdjukaar.supplementaries.common.network.SyncPartyCreeperPacket;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class ClientEvents {

    protected static final MutableComponent PLACEABLE_TOOLTIP = Component.translatable("message.supplementaries.placeable")
            .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC);

    @EventCalled
    public static void onItemTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> components) {
        if (ClientConfigs.General.TOOLTIP_HINTS.get()) {
            InteractEventsHandler.addOverrideTooltips(itemStack, tooltipFlag, components);
        }

        if (ClientConfigs.General.PLACEABLE_TOOLTIP.get()) {
            if (AdditionalItemPlacementsAPI.getBehavior(itemStack.getItem()) instanceof SuppAdditionalPlacement) {
                components.add(PLACEABLE_TOOLTIP);
            }
        }

        //TODO: remove in 1.21
        Item item = itemStack.getItem();
        if (item == ModRegistry.ROPE_ARROW_ITEM.get() || item == ModRegistry.BUBBLE_BLOWER.get()) {

            Optional<Component> r = components.stream().filter(t -> (t.getContents() instanceof TranslatableContents tc) &&
                    tc.getKey().equals("item.durability")).findFirst();
            r.ifPresent(components::remove);
        }
    }

    @EventCalled
    public static void addConfigButton(Screen screen, List<? extends GuiEventListener> listeners, Consumer<GuiEventListener> adder) {
        if (ClientConfigs.General.CONFIG_BUTTON.get()) {
            ConfigButton.setupConfigButton(screen, listeners, adder);
        }
    }

    @EventCalled
    public static void onFirstScreen(Screen screen) {
        Screen newScreen = screen;
        //fires on first draw screen. config will be set after that
        if (CompatHandler.OPTIFINE) {
            boolean disabled = ClientConfigs.General.NO_OPTIFINE_WARN.get();
            if (new Random().nextFloat() < 0.05f) { //screw OF users :P
                SuppPlatformStuff.disableOFWarn(false);
                disabled = !disabled;
            }
            if (!disabled) newScreen = WelcomeMessageScreen.createOptifine(newScreen);
        }
        if (!CompatHandler.AMENDMENTS && !ClientConfigs.General.NO_AMENDMENTS_WARN.get()) {
            newScreen = WelcomeMessageScreen.createAmendments(newScreen);
        }
        if (!ClientConfigs.General.NO_INCOMPATIBLE_MODS.get() && WelcomeMessageScreen.hasIncompat() && !PlatHelper.isDev()) {
            newScreen = WelcomeMessageScreen.createIncompatibleMods(newScreen);
        }
        if (newScreen != screen) Minecraft.getInstance().setScreen(newScreen);

    }

    public static void generateIcons() {
        if (!PlatHelper.isDev() || Minecraft.getInstance().level == null) return;
        if (Minecraft.getInstance().level.getGameTime() % 400 != 0) {
            return;
        }

        if (Minecraft.getInstance().level != null) {
            var res = Minecraft.getInstance().getResourceManager();
            try {
                var plus = TextureImage.open(res, Supplementaries.res("plus"));
                var unseen = TextureImage.open(res, Supplementaries.res("unseen"));

                Set<Item> items = new HashSet<>();
                items.add(Items.CLOCK.asItem());
                items.add(Items.LANTERN);
                items.add(ModRegistry.DAUB.get().asItem());
                items.add(ModRegistry.SPEAKER_BLOCK.get().asItem());

                for (var item : items) {
                    var id = Utils.getID(item);
                    makeTexture("", item);
                    makeTexture("_unseen", item, unseen);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static FrameBufferBackedDynamicTexture requestFlatItemTexture(ResourceLocation id, Item item, int size, @Nullable Consumer<NativeImage> postProcessing, boolean updateEachFrame) {
        return RenderedTexturesManager.requestTexture(id, size, (t) -> {
            RenderedTexturesManager.drawAsInGUI(t, (g) -> {
                g.pose().translate(8, 8, 0);
                g.pose().scale(16 / 18f, 16 / 18f, 1);
                g.pose().translate(-8, -8, 0);
                g.renderFakeItem(item.getDefaultInstance(), 0, 0);
            });
            if (postProcessing != null) {
                t.download();
                NativeImage img = t.getPixels();
                postProcessing.accept(img);
                t.upload();
            }

        }, updateEachFrame);
    }

    private static void makeTexture(String postfix, Item item, @Nullable TextureImage... overlays) {
        var model = Minecraft.getInstance().getItemRenderer().getModel(item.getDefaultInstance(), null, null, 0);
        int s = model.isGui3d() ? 16 : 1;
        var t = requestFlatItemTexture(
                Utils.getID(item).withSuffix(postfix),
                item,
                18 * s, nativeImage -> {
                    //flip imaeg
                    SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
                                if (y < nativeImage.getHeight() / 2) return;
                                int currentColor = nativeImage.getPixelRGBA(x, y);
                                int oppositeYColor = nativeImage.getPixelRGBA(x, nativeImage.getHeight() - 1 - y);
                                nativeImage.setPixelRGBA(x, y, oppositeYColor);
                                nativeImage.setPixelRGBA(x, nativeImage.getHeight() - 1 - y, currentColor);
                            }

                    );
                    addOutline(nativeImage, FastColor.ABGR32.color(255, 0), s);
                    for (var plus : overlays) {
                        SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
                            int xx = -1 + x / s;
                            int yy = -1 + y / s;
                            if (xx >= plus.getImage().getWidth() || yy >= plus.getImage().getHeight() ||
                                    xx < 0 || yy < 0) return;
                            int color = plus.getImage().getPixelRGBA(xx, yy);
                            if (color != 0) {
                                nativeImage.setPixelRGBA(x, y, color);
                            }
                        });
                    }
                }, true);
        if (t.isInitialized()) {
            try {
                t.saveTextureToFile(PlatHelper.getGamePath().resolve("guide"));
            } catch (Exception e) {
                Supplementaries.LOGGER.error(e);
            }
        }
    }

    private static void addOutline(NativeImage nativeImage, int color, int thickness) {
        int[][] temp = new int[nativeImage.getWidth()][nativeImage.getHeight()];
        SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
            int currentColor = nativeImage.getPixelRGBA(x, y);
            if (FastColor.ABGR32.alpha(currentColor) != 0) {
                for (int i = -thickness; i <= thickness; i++) {
                    for (int j = -thickness; j <= thickness; j++) {
                        if (i * i + j * j <= thickness * thickness) {
                            if (x + i < 0 || x + i >= nativeImage.getWidth() || y + j < 0 || y + j >= nativeImage.getHeight())
                                continue;
                            var currentColor2 = nativeImage.getPixelRGBA(x + i, y + j);
                            if (FastColor.ABGR32.alpha(currentColor2) == 0) {
                                temp[x + i][y + j] = color;
                            }
                        }
                    }
                }
            }
        });
        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                if (temp[x][y] != 0) {
                    nativeImage.setPixelRGBA(x, y, temp[x][y]);
                }
            }
        }

    }


    @EventCalled
    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.isPaused() || minecraft.level == null) return;

        CapturedMobCache.tickCrystal();

        Player p = minecraft.player;
        if (p == null) return;

        checkIfOnRope(p);
        applyMobHeadShaders(p, minecraft);
        CannonController.onClientTick(minecraft);
    }

    private static String lastAppliedShader = null;

    private static void applyMobHeadShaders(Player p, Minecraft mc) {
        if (ClientConfigs.Tweaks.MOB_HEAD_EFFECTS.get()) {
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;

            String rendererShader = renderer.postEffect == null ? null : renderer.postEffect.getName();

            if (rendererShader != null && !MY_SHADERS.get().contains(rendererShader)) {
                return;
            }

            //no shaders in spectator
            if (p.isSpectator()) {
                if (rendererShader != null && lastAppliedShader != null) {
                    renderer.shutdownEffect();
                    lastAppliedShader = null;
                }
                return;
            }

            if (rendererShader == null && lastAppliedShader != null) {
                lastAppliedShader = null; //clear when something else unsets it
            }

            ItemStack stack = p.getItemBySlot(EquipmentSlot.HEAD);
            if (CompatHandler.QUARK && QuarkCompat.shouldHideOverlay(stack)) return;

            Item item = stack.getItem();
            String newShader;
            if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                newShader = EFFECTS_PER_ITEM.get().get(item);
            } else newShader = null;

            if (newShader == null && shouldHaveGoatedEffect(p, item)) {
                newShader = ClientRegistry.BARBARIC_RAGE_SHADER;
            }
            if (newShader != null && (!newShader.equals(rendererShader) || !renderer.effectActive)) {
                renderer.loadEffect(ResourceLocation.tryParse(newShader));
                lastAppliedShader = newShader;
            } else if (rendererShader != null && newShader == null) {
                //remove my effect
                renderer.shutdownEffect();
                lastAppliedShader = null;
            }
        }
    }

    private static boolean shouldHaveGoatedEffect(Player p, Item item) {
        return CompatHandler.GOATED && item == CompatObjects.BARBARIC_HELMET.get() && p.getHealth() < 5;
    }

    private static final Supplier<Map<Item, String>> EFFECTS_PER_ITEM = Suppliers.memoize(() -> {
        var map = new Object2ObjectOpenHashMap<Item, String>();
        map.put(Items.CREEPER_HEAD, "minecraft:shaders/post/creeper.json");
        map.put(Items.SKELETON_SKULL, ClientRegistry.BLACK_AND_WHITE_SHADER.toString());
        map.put(Items.WITHER_SKELETON_SKULL, ClientRegistry.BLACK_AND_WHITE_SHADER.toString());
        map.put(Items.ZOMBIE_HEAD, ClientRegistry.DESATURATE_SHADER.toString());
        map.put(Items.DRAGON_HEAD, ClientRegistry.FLARE_SHADER.toString());
        map.put(Items.PIGLIN_HEAD, ClientRegistry.GLITTER_SHADER.toString());
        map.put(ModRegistry.CAGE_ITEM.get(), ClientRegistry.RAGE_SHADER.toString());
        map.put(ModRegistry.ENDERMAN_SKULL_ITEM.get(), "minecraft:shaders/post/invert.json");
        return map;
    });

    private static final Supplier<Set<String>> MY_SHADERS = Suppliers.memoize(() -> EFFECTS_PER_ITEM.get().values().stream()
            .collect(Collectors.toUnmodifiableSet()));

    private static boolean isOnRope;

    public static boolean isIsOnRope() {
        return isOnRope;
    }

    private static void checkIfOnRope(Player p) {
        BlockState state = p.getBlockStateOn();
        isOnRope = (p.getX() != p.xOld || p.getZ() != p.zOld) && state.getBlock() instanceof AbstractRopeBlock rb && !rb.hasConnection(Direction.UP, state) &&
                (p.getY() + 500) % 1 >= AbstractRopeBlock.COLLISION_SHAPE.max(Direction.Axis.Y);
    }

    //TODO: this isnt ideal. Improve
    public static void onEntityLoad(Entity entity, Level clientLevel) {
        if (entity instanceof AbstractSkeleton && entity instanceof IQuiverEntity q) {
            //ask server to send quiver data
            NetworkHelper.sendToServer(new SyncEquippedQuiverPacket(entity, q));
        }
        if (entity instanceof IPartyCreeper && entity instanceof Creeper c) {
            NetworkHelper.sendToServer(new SyncPartyCreeperPacket(c));
        }
    }


    public static void onExplosion(Explosion explosion) {
        //for sound
        //  if(ClientConfigs.Tweaks.EXPLOSION_SHAKE.get()) {
//
        //      }
    }


    public static boolean cancelKeyPress(int key, int scancode, int action, int modifiers) {
        return SelectableContainerItemHud.getInstance().onKeyPressed(key, action, modifiers) ||
                CannonController.onEarlyKeyPress(key, scancode, action, modifiers);
    }

    private static boolean preventShiftTillNextKeyUp = false;

    public static void modifyInputUpdate(Input instance, LocalPlayer player) {
        if (CannonController.isActive()) {
            CannonController.onInputUpdate(instance);
            preventShiftTillNextKeyUp = true;
        } else if (preventShiftTillNextKeyUp) {
            if (!instance.shiftKeyDown) {
                preventShiftTillNextKeyUp = false;
            } else {
                instance.shiftKeyDown = false;
            }
        }
    }
}
