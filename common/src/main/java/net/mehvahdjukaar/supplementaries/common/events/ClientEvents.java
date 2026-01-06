package net.mehvahdjukaar.supplementaries.common.events;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.MobHeadShadersManager;
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
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;


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
        /*
        if (!CompatHandler.AMENDMENTS && !ClientConfigs.General.NO_AMENDMENTS_WARN.get()) {
            newScreen = WelcomeMessageScreen.createAmendments(newScreen);
        }*/
        if (!ClientConfigs.General.NO_INCOMPATIBLE_MODS.get() && WelcomeMessageScreen.hasIncompat() && !PlatHelper.isDev()) {
            newScreen = WelcomeMessageScreen.createIncompatibleMods(newScreen);
        }
        if (newScreen != screen) Minecraft.getInstance().setScreen(newScreen);

    }

    @EventCalled()
    public static boolean onMouseScrolled(double dy) {
        if (SelectableContainerItemHud.getInstance().onMouseScrolled(dy)) {
           return true;
        }
        Level l;
        l.dimensionType().fixedTime();
                l.dayTime();
        if (CannonController.onMouseScrolled(dy)) {
            return true;
        }

        return false;
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


    @EventCalled
    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.isPaused() || minecraft.level == null) return;

        CapturedMobCache.tickCrystal();

        Player p = minecraft.player;
        if (p == null) return;

        checkIfOnRope(p);
        MobHeadShadersManager.INSTANCE.applyMobHeadShaders(p, minecraft);
        CannonController.onClientTick(minecraft);
    }

    private static boolean isOnRope;

    private static double wobble; // from 0 to 1
    public static double getRopeWobble(double partialTicks) {
        Player p = Minecraft.getInstance().player;
        if (p != null && !Minecraft.getInstance().isPaused() && !p.isSpectator()) {
            if (isOnRope || wobble != 0) {
                double period = ClientConfigs.Blocks.ROPE_WOBBLE_PERIOD.get();
                double newWobble = (((p.tickCount + partialTicks) / period) % 1);
                if (!isOnRope && newWobble < wobble) {
                    wobble = 0;
                } else {
                    wobble = newWobble;
                }
                return Mth.sin((float) (wobble * 2 * Math.PI)) * ClientConfigs.Blocks.ROPE_WOBBLE_AMPLITUDE.get();
            }
        }
        return 0;
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
