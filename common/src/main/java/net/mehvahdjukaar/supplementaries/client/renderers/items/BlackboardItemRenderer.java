package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardManager;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;


public class BlackboardItemRenderer extends ItemStackRenderer {
    private static final BlockState STATE = ModRegistry.BLACKBOARD.get().defaultBlockState();

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0,0,-0.34375);
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var model = ClientPlatformHelper.getModel(blockRenderer.getBlockModelShaper().getModelManager(),
                 ClientRegistry.BLACKBOARD_FRAME);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(STATE, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        blockRenderer.getModelRenderer().renderModel(matrixStackIn.last(), bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(STATE, false)),
                STATE, model, 1, 1, 1, combinedLightIn, combinedOverlayIn);

        CompoundTag com = stack.getTagElement("BlockEntityTag");
        long[] packed = new long[16];
        if(com != null && com.contains("Pixels")) {
            packed = com.getLongArray("Pixels");
        }
        var blackboard = BlackboardManager.getBlackboardInstance(BlackboardManager.BlackboardKey.of(packed));
        VertexConsumer builder = bufferIn.getBuffer(blackboard.getRenderType());

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff'; // ok

        matrixStackIn.mulPose(RotHlpr.Y180);
        matrixStackIn.translate(-1, 0, -0.6875);
        VertexUtils.addQuadSide(builder, matrixStackIn, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1);

        matrixStackIn.popPose();

    }
}