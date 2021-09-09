package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SlingshotItemRenderer extends ItemStackTileEntityRenderer {

    private final ResourceLocation SIMPLE_MODEL = new ModelResourceLocation("supplementaries:slingshot_base#");

    private ItemRenderer itemRenderer;

    public SlingshotItemRenderer() {
    }

    public IBakedModel getSimpleModel(ItemStack stack, @Nullable LivingEntity entity){

        IBakedModel baseModel = this.itemRenderer.getItemModelShaper().getModelManager().getModel(SIMPLE_MODEL);

        IBakedModel ibakedmodel = baseModel.getOverrides().resolve(baseModel, stack, null, entity);
        return ibakedmodel == null ? this.itemRenderer.getItemModelShaper().getModelManager().getMissingModel() : ibakedmodel;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transform, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        //main model

        if(this.itemRenderer == null){
            this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        }
        IBakedModel ibakedmodel = this.getSimpleModel(stack, null);

        itemRenderer.render(stack, transform, true, matrixStack, buffer, light,overlay, ibakedmodel);

        matrixStack.pushPose();
        matrixStack.translate(0.5D, 0.5D, 0.5D);


        matrixStack.popPose();

    }

}
