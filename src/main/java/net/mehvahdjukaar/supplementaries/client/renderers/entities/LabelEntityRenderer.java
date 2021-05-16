package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.entities.LabelEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.function.Consumer;


public class LabelEntityRenderer extends EntityRenderer<LabelEntity> {
    public static final StateContainer<Block, BlockState> LABEL_FAKE_DEFINITION = (new StateContainer.Builder<Block, BlockState>(Blocks.AIR)).add(BooleanProperty.create("jar")).create(Block::defaultBlockState, BlockState::new);

    private static final ModelResourceLocation LABEL_LOCATION = new ModelResourceLocation(Supplementaries.MOD_ID+":label", "jar=false");
    private static final ModelResourceLocation JAR_LABEL_LOCATION = new ModelResourceLocation(Supplementaries.MOD_ID+":label", "jar=true");
    private final ItemRenderer itemRenderer;
    private final BlockModelRenderer modelRenderer;
    private final ModelManager modelManager;

    public LabelEntityRenderer(EntityRendererManager manager, ItemRenderer itemRenderer) {
        super(manager);
        this.itemRenderer = itemRenderer;
        Minecraft minecraft = Minecraft.getInstance();
        this.modelRenderer = minecraft.getBlockRenderer().getModelRenderer();
        this.modelManager = minecraft.getBlockRenderer().getBlockModelShaper().getModelManager();
    }

    public void render(LabelEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, light);
        matrixStack.pushPose();
        Direction direction = entity.getDirection();
        Vector3d vector3d = this.getRenderOffset(entity, partialTicks);
        matrixStack.translate(-vector3d.x(), -vector3d.y(), -vector3d.z());

        double d0 = 0.46875D;
        matrixStack.translate((double)direction.getStepX() * d0, (double)direction.getStepY() * d0, (double)direction.getStepZ() * d0);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(entity.xRot));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.yRot));

        matrixStack.pushPose();
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        modelRenderer.renderModel(matrixStack.last(), buffer.getBuffer(Atlases.solidBlockSheet()), null, modelManager.getModel(LABEL_LOCATION), 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();

        ItemStack itemstack = new ItemStack(Items.OAK_LOG);//entity.getItem();
        if (!itemstack.isEmpty()) {

            matrixStack.translate(0.0D, 0.0D, 0.5-0.013);

            matrixStack.scale(0.5F, 0.5F, 0.5F);
            renderFlatItem(itemstack,matrixStack,buffer,light,OverlayTexture.NO_OVERLAY);
        }

        matrixStack.popPose();

        //hax
        matrixStack.popPose();
        RenderHelper.setupLevel(matrixStack.last().pose());
        matrixStack.pushPose();
    }


    private void renderFlatItem(@Nonnull ItemStack itemStack, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        matrix.pushPose();
        //matrix.translate(0.5D, 0.5, 1);
        matrix.scale(-1, 1, 0.0008F);

        Consumer<IRenderTypeBuffer> finish = (buf) -> {
            if (buf instanceof IRenderTypeBuffer.Impl) {
                ((IRenderTypeBuffer.Impl)buf).endBatch();
            }
        };
        try {
            IBakedModel itemModel = itemRenderer.getModel(itemStack, null, null);
            finish.accept(buffer);
            if (itemModel.isGui3d()) {
                RenderHelper.setupFor3DItems();
            } else {
                RenderHelper.setupForFlatItems();
            }
            matrix.last().normal().load(Matrix3f.createScaleMatrix(1.0F, -1.0F, 1.0F));
            itemRenderer.render(itemStack, ItemCameraTransforms.TransformType.GUI, false, matrix, buffer, combinedLight, combinedOverlay, itemModel);
            finish.accept(buffer);
        } catch (Exception ignored) {}

        matrix.popPose();

    }

    public Vector3d getRenderOffset(LabelEntity entity, float partialTicks) {
        return new Vector3d((float)entity.getDirection().getStepX() * 0.3F, -0.25D, (float)entity.getDirection().getStepZ() * 0.3F);
    }

    public ResourceLocation getTextureLocation(LabelEntity p_110775_1_) {
        return AtlasTexture.LOCATION_BLOCKS;
    }

    protected boolean shouldShowName(LabelEntity p_177070_1_) {
        if (Minecraft.renderNames() && !p_177070_1_.getItem().isEmpty() && p_177070_1_.getItem().hasCustomHoverName() && this.entityRenderDispatcher.crosshairPickEntity == p_177070_1_) {
            double d0 = this.entityRenderDispatcher.distanceToSqr(p_177070_1_);
            float f = p_177070_1_.isDiscrete() ? 32.0F : 64.0F;
            return d0 < (double)(f * f);
        } else {
            return false;
        }
    }

}

