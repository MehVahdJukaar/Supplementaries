package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.entities.MashlingEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class MashlingEntityRenderer extends MobRenderer<MashlingEntity, MashlingModel> {
    public MashlingEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MashlingModel(), 0.3F);
    }

    @Override
    public void render(MashlingEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(MashlingEntity entity) {
        return Textures.YELLOW_CONCRETE_TEXTURE;
    }
}
