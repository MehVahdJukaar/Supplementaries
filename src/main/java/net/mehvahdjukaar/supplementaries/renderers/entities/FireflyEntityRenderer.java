package net.mehvahdjukaar.supplementaries.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.renderers.Const;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class FireflyEntityRenderer extends EntityRenderer<FireflyEntity> {
    public FireflyEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(FireflyEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                       int packedLightIn) {

        matrixStackIn.push();

        float r = CommonUtil.ishalloween?0.3f:1;
        float g = CommonUtil.ishalloween?0:1;
        float b = 1;
        float a = MathHelper.lerp(partialTicks, entityIn.prevAlpha, entityIn.alpha);

        matrixStackIn.translate(0.0D, 0.5, 0.0D);
        matrixStackIn.rotate(this.renderManager.getCameraOrientation());
        matrixStackIn.rotate(Const.Y180);
        float scale = (float) ClientConfigs.cached.FIREFLY_SCALE;
        matrixStackIn.scale(a*scale, a*scale, a*scale);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getBeaconBeam(Resources.FIREFLY_TEXTURE, true));

        RendererUtil.addQuadSide(builder, matrixStackIn, -0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f, 0, 0, 1, 1, r, g,b, a, 240, 0, 0, 1, 0);

        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }


    @Override
    public ResourceLocation getEntityTexture(FireflyEntity entity) {
        return Resources.FIREFLY_TEXTURE;
    }
}
