package net.mehvahdjukaar.supplementaries.common.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.MobHeadShadersManager;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.client.screens.ConfigButton;
import net.mehvahdjukaar.supplementaries.client.screens.WelcomeMessageScreen;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeBlock;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.SyncEquippedQuiverPacket;
import net.mehvahdjukaar.supplementaries.common.network.SyncPartyCreeperPacket;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ClientEvents {

    protected static final MutableComponent PLACEABLE_TOOLTIP = Component.translatable("message.supplementaries.placeable")
            .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC);

    @EventCalled
    public static void onItemTooltip(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> components) {
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


    @EventCalled
    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.isPaused() || minecraft.level == null) return;

        CapturedMobCache.tickCrystal();

        Player p = minecraft.player;
        if (p == null) return;

        checkIfOnRope(p);
      MobHeadShadersManager.INSTANCE. applyMobHeadShaders(p, minecraft);
        CannonController.onClientTick(minecraft);
    }

    private static boolean isOnRope;

    public static boolean isIsOnRope() {
        return isOnRope;
    }

    private static void checkIfOnRope(Player p) {
        BlockState state = p.getFeetBlockState();
        isOnRope = (p.getX() != p.xOld || p.getZ() != p.zOld) && state.getBlock() instanceof AbstractRopeBlock rb && !rb.hasConnection(Direction.UP, state) &&
                (p.getY() + 500) % 1 >= AbstractRopeBlock.COLLISION_SHAPE.max(Direction.Axis.Y);
    }

    //TODO: this isnt ideal. Improve
    public static void onEntityLoad(Entity entity, Level clientLevel) {
        if (entity instanceof AbstractSkeleton && entity instanceof IQuiverEntity q) {
            //ask server to send quiver data
            ModNetwork.CHANNEL.sendToServer(new SyncEquippedQuiverPacket(entity, q));
        }
        if (entity instanceof IPartyCreeper && entity instanceof Creeper c) {
            ModNetwork.CHANNEL.sendToServer(new SyncPartyCreeperPacket(c));
        }
    }


    public static void onExplosion(Explosion explosion) {
        //  if(ClientConfigs.Tweaks.EXPLOSION_SHAKE.get()) {
//
        //      }
    }
}
