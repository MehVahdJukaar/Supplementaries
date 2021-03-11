package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if((event.getPlayer()==null) || (event.getPlayer().world==null) ||
                !ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().gameSettings.advancedItemTooltips)return;
        Item i = event.getItemStack().getItem();
        if(ServerConfigs.cached.WALL_LANTERN_PLACEMENT && CommonUtil.isLantern(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.wall_lantern").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
        }
        else if(ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && CommonUtil.isBrick(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.throwable_brick").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
        }
        else if(ServerConfigs.cached.HANGING_POT_PLACEMENT && CommonUtil.isPot(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.hanging_pot").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
        }
        else if(ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT && CommonUtil.isCake(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.double_cake").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
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


}
