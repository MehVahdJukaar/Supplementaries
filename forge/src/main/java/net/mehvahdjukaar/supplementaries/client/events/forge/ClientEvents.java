package net.mehvahdjukaar.supplementaries.client.events.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkTooltipPlugin;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {

        if ((event.getPlayer() != null)) {
            event.getPlayer();

            if (ClientConfigs.cached.TOOLTIP_HINTS && event.getFlags().isAdvanced()) {
                ItemsOverrideHandler.addOverrideTooltips(event);
            }

            if (CompatHandler.quark) {
                QuarkTooltipPlugin.onItemTooltipEvent(event);
            }

            Item item = event.getItemStack().getItem();
            if (item == ModRegistry.ROPE_ARROW_ITEM.get() || item == ModRegistry.BUBBLE_BLOWER.get()) {
                List<Component> tooltip = event.getToolTip();

                Optional<Component> r = tooltip.stream().filter(t -> (t.getContents() instanceof TranslatableContents tc) &&
                        tc.getKey().equals("item.durability")).findFirst();
                r.ifPresent(tooltip::remove);
            }
        }
    }




    private static float partialTicks;

    public static float getPartialTicks() {
        return partialTicks;
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            partialTicks = event.renderTickTime;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
            CapturedMobCache.tickCrystal();
            Player p = Minecraft.getInstance().player;
            if (p != null) {
                BlockState state = p.getFeetBlockState();
                isOnRope = (p.getX() != p.xOld || p.getZ() != p.zOld) && state.is(ModRegistry.ROPE.get()) && !state.getValue(RopeBlock.UP) &&
                        (p.getY() + 500) % 1 >= RopeBlock.COLLISION_SHAPE.max(Direction.Axis.Y);
            }
        }
    }

    private static double wobble; // from 0 to 1
    private static boolean isOnRope;

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Player p = Minecraft.getInstance().player;
        if (p != null && !Minecraft.getInstance().isPaused()) {
            if (isOnRope || wobble != 0) {
                double period = ClientConfigs.cached.ROPE_WOBBLE_PERIOD;
                double newWobble = (((p.tickCount + event.getPartialTick()) / period) % 1);
                if (!isOnRope && newWobble < wobble) {
                    wobble = 0;
                } else {
                    wobble = newWobble;
                }
                event.setRoll(event.getRoll() + Mth.sin((float) (wobble * 2 * Math.PI)) * ClientConfigs.cached.ROPE_WOBBLE_AMPLITUDE);
            }
        }
    }


    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.InitScreenEvent event) {
        if (ClientConfigs.cached.CONFIG_BUTTON && CompatHandler.configured) {
            ConfigButton.setupConfigButton(event);
        }
    }


}
