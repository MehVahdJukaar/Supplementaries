package net.mehvahdjukaar.supplementaries.world.data.map.lib.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.MapData;

public class DecorationRenderer<T extends CustomDecoration> {
    private final RenderType RENDER_TYPE;
    private final int mapColor;
    private final boolean renderOnFrame;

    public DecorationRenderer(ResourceLocation texture,  int mapColor, boolean renderOnFrame){
        this.RENDER_TYPE = RenderType.text(texture);
        this.renderOnFrame = renderOnFrame;
        this.mapColor = mapColor;
    }
    public DecorationRenderer(ResourceLocation texture, int mapColor){
        this(texture,mapColor,true);
    }

    public DecorationRenderer(ResourceLocation texture){
        this(texture,-1,true);
    }

    public int getMapColor(T decoration) {
        return mapColor;
    }

    public RenderType getRenderType(T decoration) {
        return RENDER_TYPE;
    }

    public boolean render(T decoration, MatrixStack matrixStack, IRenderTypeBuffer buffer, MapData mapData, boolean isOnFrame, int light, int index) {
        if (!isOnFrame || renderOnFrame) {

            matrixStack.pushPose();
            matrixStack.translate(0.0F + (float) decoration.getX() / 2.0F + 64.0F, 0.0F + (float) decoration.getY() / 2.0F + 64.0F, -0.02F);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) (decoration.getRot() * 360) / 16.0F));
            matrixStack.scale(4.0F, 4.0F, 3.0F);
            matrixStack.translate(-0.125D, 0.125D, 0.0D);

            Matrix4f matrix4f1 = matrixStack.last().pose();

            IVertexBuilder vertexBuilder = buffer.getBuffer(this.getRenderType(decoration));

            int color = this.getMapColor(decoration);

            int b = NativeImage.getR(color);
            int g = NativeImage.getG(color);
            int r = NativeImage.getB(color);

            vertexBuilder.vertex(matrix4f1, -1.0F, 1.0F, (float) index * -0.001F).color(r, g, b, 255).uv(0, 1).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, 1.0F, 1.0F, (float) index * -0.001F).color(r, g, b, 255).uv(1, 1).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, 1.0F, -1.0F, (float) index * -0.001F).color(r, g, b, 255).uv(1, 0).uv2(light).endVertex();
            vertexBuilder.vertex(matrix4f1, -1.0F, -1.0F, (float) index * -0.001F).color(r, g, b, 255).uv(0, 0).uv2(light).endVertex();
            matrixStack.popPose();
            if (decoration.getDisplayName() != null) {
                FontRenderer fontrenderer = Minecraft.getInstance().font;
                ITextComponent itextcomponent = decoration.getDisplayName();
                float f6 = (float) fontrenderer.width(itextcomponent);
                float f7 = MathHelper.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
                matrixStack.pushPose();
                matrixStack.translate((double) (0.0F + (float) decoration.getX() / 2.0F + 64.0F - f6 * f7 / 2.0F), (double) (0.0F + (float) decoration.getY() / 2.0F + 64.0F + 4.0F), (double) -0.025F);
                matrixStack.scale(f7, f7, 1.0F);
                matrixStack.translate(0.0D, 0.0D, (double) -0.1F);
                fontrenderer.drawInBatch(itextcomponent, 0.0F, 0.0F, -1, false, matrixStack.last().pose(), buffer, false, Integer.MIN_VALUE, light);
                matrixStack.popPose();
            }
            return true;
        }
        return false;
    }
}
