package net.mehvahdjukaar.supplementaries.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.ConfigButton;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkTooltipPlugin;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if ((event.getPlayer() != null) && (event.getPlayer().level != null)) {

            if (ClientConfigs.cached.TOOLTIP_HINTS && Minecraft.getInstance().options.advancedItemTooltips) {
                ItemsOverrideHandler.addOverrideTooltips(event);
            }

            if (CompatHandler.quark) {
                QuarkTooltipPlugin.onItemTooltipEvent(event);
            }

            if(event.getItemStack().getItem() == ModRegistry.ROPE_ARROW_ITEM.get()){
                List<ITextComponent> tooltip = event.getToolTip();
                Optional<ITextComponent> r = tooltip.stream().filter(t-> (t instanceof TranslationTextComponent) && ((TranslationTextComponent) t)
                        .getKey().equals("item.durability")).findFirst();
                r.ifPresent(tooltip::remove);
            }
        }
    }

    @SubscribeEvent
    public static void renderTooltipEvent(RenderTooltipEvent.PostText event) {
        ItemStack stack = event.getStack();
        Item i = stack.getItem();
        if (CompatHandler.quark) {
            if (i == ModRegistry.SACK_ITEM.get()) {
                QuarkTooltipPlugin.renderTooltipEvent(event);
            } else if (i == ModRegistry.SAFE_ITEM.get()) {
                QuarkTooltipPlugin.renderTooltipEvent(event);
            }
        }
        if (i == ModRegistry.BLACKBOARD_ITEM.get()) {
            CompoundNBT cmp = stack.getTagElement("BlockEntityTag");
            if (cmp != null && cmp.contains("Pixels")) {
                long[] packed = cmp.getLongArray("Pixels");

                //credits to quark. Uses same code so it's consistent with map preview
                Minecraft mc = Minecraft.getInstance();

                int pad = 7;
                float size = 135.0F;
                float scale = 0.5F;

                MatrixStack matrixStack = event.getMatrixStack();
                RenderSystem.color3f(1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bind(BlackboardTextureManager.INSTANCE.getResourceLocation(packed));
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuilder();

                matrixStack.translate(event.getX(), (float) event.getY() - size * scale - 5.0F, 500.0D);
                matrixStack.scale(scale, scale, 1.0F);
                RenderSystem.enableBlend();
                Matrix4f mat = matrixStack.last().pose();

                //AbstractGui.blit(matrix, x, y, 0.0F, 0.0F, 1*width, 1*width, 16*width, 16*width);
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.vertex(mat, (float) (-pad), size, 0.0F).uv(0.0F, 1.0F).endVertex();
                buffer.vertex(mat, size, size, 0.0F).uv(1.0F, 1.0F).endVertex();
                buffer.vertex(mat, size, (float) (-pad), 0.0F).uv(1.0F, 0.0F).endVertex();
                buffer.vertex(mat, (float) (-pad), (float) (-pad), 0.0F).uv(0.0F, 0.0F).endVertex();
                tessellator.end();
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
        if (!ClientConfigs.cached.CONFIG_BUTTON) return;
        if (!CompatHandler.configured) return;
        ConfigButton.setupConfigButton(event);

    }

}
