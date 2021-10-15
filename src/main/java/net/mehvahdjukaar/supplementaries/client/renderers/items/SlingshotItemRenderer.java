package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class SlingshotItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final ResourceLocation SIMPLE_MODEL = new ModelResourceLocation("supplementaries:slingshot_base#");

    private ItemRenderer itemRenderer;

    public SlingshotItemRenderer() {
    }

    public BakedModel getSimpleModel(ItemStack stack, @Nullable LivingEntity entity){

        BakedModel baseModel = this.itemRenderer.getItemModelShaper().getModelManager().getModel(SIMPLE_MODEL);

        BakedModel ibakedmodel = baseModel.getOverrides().resolve(baseModel, stack, null, entity);
        return ibakedmodel == null ? this.itemRenderer.getItemModelShaper().getModelManager().getMissingModel() : ibakedmodel;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transform, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        //main model

        if(this.itemRenderer == null){
            this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        }
        BakedModel ibakedmodel = this.getSimpleModel(stack, null);

        itemRenderer.render(stack, transform, true, matrixStack, buffer, light,overlay, ibakedmodel);

        matrixStack.pushPose();
        matrixStack.translate(0.5D, 0.5D, 0.5D);


        matrixStack.popPose();

    }

}
