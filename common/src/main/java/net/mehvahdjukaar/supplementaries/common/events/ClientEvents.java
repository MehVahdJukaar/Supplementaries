package net.mehvahdjukaar.supplementaries.common.events;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.client.screens.ConfigButton;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventOverrideHandler;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.SyncSkellyQuiverPacket;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;


public class ClientEvents {

    @EventCalled
    public static void onItemTooltip(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> components) {
        if (ClientConfigs.General.TOOLTIP_HINTS.get() && tooltipFlag.isAdvanced()) {
            InteractEventOverrideHandler.addOverrideTooltips(itemStack, tooltipFlag, components);
        }

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
    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.isPaused() || minecraft.level == null) return;
        CapturedMobCache.tickCrystal();
        Player p = minecraft.player;
        if (p == null) return;
        BlockState state = p.getFeetBlockState();
        isOnRope = (p.getX() != p.xOld || p.getZ() != p.zOld) && state.is(ModRegistry.ROPE.get()) && !state.getValue(RopeBlock.UP) &&
                (p.getY() + 500) % 1 >= RopeBlock.COLLISION_SHAPE.max(Direction.Axis.Y);

        if (ClientConfigs.Tweaks.MOB_HEAD_EFFECTS.get()) {
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;

            String current = renderer.postEffect == null ? null : renderer.postEffect.getName();

            ItemStack stack = p.getItemBySlot(EquipmentSlot.HEAD);
            if (CompatHandler.QUARK && QuarkCompat.shouldHideOverlay(stack)) return;
            Item item = stack.getItem();
            String newShader = EFFECTS_PER_ITEM.get(item);
            if (newShader == null && CompatHandler.GOATED && item == CompatObjects.BARBARIC_HELMET.get()) {
                if (p.getHealth() < 5) {
                    newShader = ClientRegistry.BARBARIC_RAGE_SHADER;
                }
            }
            if (newShader != null && !newShader.equals(current)) {
                renderer.loadEffect(new ResourceLocation(newShader));
            } else if (newShader == null && EFFECTS_PER_ITEM.containsValue(current)) {
                renderer.shutdownEffect();
            }
        }

        //forge handles key up with event
        if (QuiverArrowSelectGui.isUsingKey() && !ClientRegistry.QUIVER_KEYBIND.isUnbound()) {
            //handles release edge cases
            QuiverArrowSelectGui.setUsingKeybind(InputConstants.isKeyDown(
                    Minecraft.getInstance().getWindow().getWindow(),
                    ClientRegistry.QUIVER_KEYBIND.key.getValue()
            ));
        }
    }

    private static final Map<Item, String> EFFECTS_PER_ITEM = Util.make(() -> {
        var map = new Object2ObjectOpenHashMap<Item, String>();
        map.put(Items.CREEPER_HEAD, "minecraft:shaders/post/creeper.json");
        map.put(Items.SKELETON_SKULL, ClientRegistry.BLACK_AND_WHITE_SHADER.toString());
        map.put(Items.WITHER_SKELETON_SKULL, ClientRegistry.BLACK_AND_WHITE_SHADER.toString());
        map.put(Items.ZOMBIE_HEAD, "minecraft:shaders/post/desaturate.json");
        map.put(Items.DRAGON_HEAD, ClientRegistry.FLARE_SHADER.toString());
        map.put(ModRegistry.CAGE_ITEM.get(), ClientRegistry.RAGE_SHADER.toString());
        map.put(ModRegistry.ENDERMAN_SKULL_ITEM.get(), "minecraft:shaders/post/invert.json");

        return map;
    });

    private static boolean isOnRope;

    public static boolean isIsOnRope() {
        return isOnRope;
    }


    public static void onEntityLoad(Entity entity, Level clientLevel) {
        if (entity instanceof AbstractSkeleton q && entity instanceof IQuiverEntity) {
            //ask server to send quiver data
            NetworkHandler.CHANNEL.sendToServer(new SyncSkellyQuiverPacket(q));
        }
    }
}
