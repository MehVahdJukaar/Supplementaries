package net.mehvahdjukaar.supplementaries.common.events;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.ConfigButton;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkTooltipPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
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
                Optional<Component> r = tooltip.stream().filter(t -> (t instanceof TranslatableComponent component) &&
                        component.getKey().equals("item.durability")).findFirst();
                r.ifPresent(tooltip::remove);
            }
        }
    }


    //enderman hold block in rain
    /*
    @SubscribeEvent
    public static void onRenderEnderman(RenderLivingEvent<EndermanEntity, EndermanModel<EndermanEntity>> event) {
        if(event.getEntity()instanceof EndermanEntity){
            LivingRenderer<EndermanEntity, EndermanModel<EndermanEntity>> renderer = event.getRenderer();
            if(renderer instanceof EndermanRenderer) {
                MatrixStack matrixStack = event.getMatrixStack();
                matrixStack.push();

                //renderer.getEntityModel().bipedLeftArm.showModel=false;

                //event.getRenderer().getEntityModel().bipedLeftArm.rotateAngleX=180;


                event.getRenderer().getEntityModel().bipedLeftArm.showModel=true;
                //bipedRightArm.rotateAngleX=100;
                int i = getPackedOverlay(event.getEntity(), 0);
                //event.getRenderer().getEntityModel().bipedLeftArm.render(event.getMatrixStack(),event.getBuffers().getBuffer(RenderType.getEntityCutout(new ResourceLocation("textures/entity/enderman/enderman.png"))), event.getLight(),i);
                event.getRenderer().getEntityModel().bipedLeftArm.showModel=false;
                matrixStack.pop();
            }
        }
    }*/
    /*
    @SubscribeEvent
    public static void onRenderEnderman(PlayerInteractEvent.EntityInteractSpecific event) {

        Entity e = event.getTarget();
        if(e instanceof MobEntity && event.getItemStack().getItem() instanceof CompassItem){
            ((MobEntity) e).setHomePosAndDistance(new BlockPos(0,63,0),100);
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
        }
    }*/


    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.InitScreenEvent event) {
        if (ClientConfigs.cached.CONFIG_BUTTON && CompatHandler.configured) {
            ConfigButton.setupConfigButton(event);
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
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null){
            CapturedMobCache.tickCrystal();

        }
    }


}
