package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.ConfigButton;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkTooltipPlugin;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item i = event.getItemStack().getItem();
        List<ITextComponent> tooltip = event.getToolTip();
         if((event.getPlayer()!=null) && (event.getPlayer().level!=null) && ClientConfigs.cached.TOOLTIP_HINTS
                && Minecraft.getInstance().options.advancedItemTooltips){
            if (ServerConfigs.cached.WALL_LANTERN_PLACEMENT && CommonUtil.isLantern(i)) {
                tooltip.add(new TranslationTextComponent("message.supplementaries.wall_lantern").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
            } else if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && CommonUtil.isBrick(i)) {
                tooltip.add(new TranslationTextComponent("message.supplementaries.throwable_brick").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
            } else if (ServerConfigs.cached.HANGING_POT_PLACEMENT && CommonUtil.isPot(i)) {
                tooltip.add(new TranslationTextComponent("message.supplementaries.hanging_pot").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
            } else if (ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT && CommonUtil.isCake(i)) {
                tooltip.add(new TranslationTextComponent("message.supplementaries.double_cake").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
            } else if ((ServerConfigs.cached.PLACEABLE_STICKS && i == Items.STICK) ||
                    (ServerConfigs.cached.PLACEABLE_RODS && i == Items.BLAZE_ROD)) {
                tooltip.add(new TranslationTextComponent("message.supplementaries.sticks").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
            }
        }
         if(CompatHandler.quark) {
             QuarkTooltipPlugin.onItemTooltipEvent(event);
         }

    }

    @SubscribeEvent
    public static void renderTooltipEvent(RenderTooltipEvent.PostText event) {
        if(CompatHandler.quark){
            Item i = event.getStack().getItem();
            if(i == Registry.SACK_ITEM.get()){
                QuarkTooltipPlugin.renderTooltipEvent(event);
            }
            else if(i == Registry.SAFE_ITEM.get()){
                QuarkTooltipPlugin.renderTooltipEvent(event);
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
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
        if(!ClientConfigs.cached.CONFIG_BUTTON)return;
        if(!CompatHandler.configured)return;
        ConfigButton.setupConfigButton(event);

    }

}
