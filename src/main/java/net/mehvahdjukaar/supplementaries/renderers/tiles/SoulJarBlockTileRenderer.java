package net.mehvahdjukaar.supplementaries.renderers.tiles;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.tiles.FireflyJarBlockTile;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.EndPortalTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class SoulJarBlockTileRenderer extends TileEntityRenderer<FireflyJarBlockTile> {


    public SoulJarBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }
/*
    public static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    public static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final List<RenderType> RENDER_TYPES = IntStream.range(0, 16).mapToObj((i) -> RenderType.getEndPortal(i + 1)).collect(ImmutableList.toImmutableList());


    protected int getPasses(double p_191286_1_) {
        if (p_191286_1_ > 36864.0D) {
            return 1;
        } else if (p_191286_1_ > 25600.0D) {
            return 3;
        } else if (p_191286_1_ > 16384.0D) {
            return 5;
        } else if (p_191286_1_ > 9216.0D) {
            return 7;
        } else if (p_191286_1_ > 4096.0D) {
            return 9;
        } else if (p_191286_1_ > 1024.0D) {
            return 11;
        } else if (p_191286_1_ > 576.0D) {
            return 13;
        } else {
            return p_191286_1_ > 256.0D ? 14 : 15;
        }
    }



    private void renderCube(float p_228883_2_, float p_228883_3_, Matrix4f p_228883_4_, IVertexBuilder p_228883_5_) {
        float f = 0.0001f;//(RANDOM.nextFloat() * 0.5F + 0.1F) * p_228883_3_;
        float f1 = 0.005f;//(RANDOM.nextFloat() * 0.5F + 0.4F) * p_228883_3_;
        float f2 = 0.5f;//(RANDOM.nextFloat() * 0.5F + 0.5F) * p_228883_3_;
        //this.renderFace( p_228883_4_, p_228883_5_, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, f, f1, f2, Direction.SOUTH);
        //this.renderFace( p_228883_4_, p_228883_5_, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f, f1, f2, Direction.NORTH);
        //this.renderFace( p_228883_4_, p_228883_5_, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, f, f1, f2, Direction.EAST);
        //this.renderFace( p_228883_4_, p_228883_5_, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, f, f1, f2, Direction.WEST);
        this.renderFace( p_228883_4_, p_228883_5_, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f, f1, f2, Direction.DOWN);
        //this.renderFace( p_228883_4_, p_228883_5_, 0.0F, 1.0F, p_228883_2_, p_228883_2_, 1.0F, 1.0F, 0.0F, 0.0F, f, f1, f2, Direction.UP);
    }

    private void renderFace(Matrix4f matrix4f, IVertexBuilder builder, float p_228884_4_, float p_228884_5_, float p_228884_6_, float p_228884_7_, float p_228884_8_, float p_228884_9_, float p_228884_10_, float p_228884_11_, float r, float g, float b, Direction p_228884_15_) {

        builder.pos(matrix4f, p_228884_4_, p_228884_6_, p_228884_8_).color(r, g, b, 1.0F).endVertex();
        builder.pos(matrix4f, p_228884_5_, p_228884_6_, p_228884_9_).color(r, g, b, 1.0F).endVertex();
        builder.pos(matrix4f, p_228884_5_, p_228884_7_, p_228884_10_).color(r, g, b, 1.0F).endVertex();
        builder.pos(matrix4f, p_228884_4_, p_228884_7_, p_228884_11_).color(r, g, b, 1.0F).endVertex();


    }*/

    @Override
    public void render(FireflyJarBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        /*
        matrixStackIn.translate(0,-0.3,0);
        RANDOM.setSeed(31100L);
        double d0 = tile.getPos().distanceSq(this.renderDispatcher.renderInfo.getProjectedView(), true);
        int i = 3;//this.getPasses(d0);
        float f = 0.75F;
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        this.renderCube(f, 0.15F, matrix4f, bufferIn.getBuffer(RENDER_TYPES.get(0)));

        for(int j = 1; j < i; ++j) {
            //this.renderCube( f, 2.0F / (float)(18 - j), matrix4f, bufferIn.getBuffer(RENDER_TYPES.get(j)));
        }
        //this.renderFace(matrix4f, bufferIn.getBuffer(RENDER_TYPES.get(5)), 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1, 1, 1, Direction.SOUTH);

        RenderState.TransparencyState st = new RenderState.TransparencyState("additive_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });
        RenderState.TextureState text = new RenderState.TextureState(EndPortalTileEntityRenderer.END_PORTAL_TEXTURE, false, false);

        RenderType t =RenderType.makeType("end_portal", DefaultVertexFormats.POSITION_COLOR, 7, 256,
                false, true, RenderType.State.getBuilder().transparency(st).texture(text)
                        .texturing(new PortalTexturingState(5)).build(false));


        this.renderFace( matrix4f, bufferIn.getBuffer(t), 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1, 1, 1, Direction.DOWN);


        */









        if(!tile.soul || Minecraft.getInstance().gameSettings.graphicFanciness== GraphicsFanciness.FABULOUS)return;
        matrixStackIn.push();
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff'; // ok

        matrixStackIn.translate(0.5, 0.5-0.125, 0.5);
        matrixStackIn.scale(0.5f, 0.5f, 0.5f);

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(Resources.SOUL_TEXTURE);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getCutout());

        Quaternion rotation = Minecraft.getInstance().getRenderManager().getCameraOrientation();
        matrixStackIn.rotate(rotation);

        RendererUtil.addQuadSide(builder, matrixStackIn, 0.5f, -0.5f, 0, -0.5f, 0.5f, 0, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(),  1,  1, 1, 1, lu, lv, 0, 1, 0);

        matrixStackIn.pop();


    }

/*
    @OnlyIn(Dist.CLIENT)
    public static final class PortalTexturingState extends RenderState.TexturingState {
        private final int iteration;

        public PortalTexturingState(int it) {
            super("portal_texturing", () -> {
                //RenderSystem.matrixMode(5890);
                RenderSystem.matrixMode(5889);

                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();

                RenderSystem.multMatrix(Matrix4f.perspective(85.0D, (float)Minecraft.getInstance().getMainWindow().getFramebufferWidth() / (float)Minecraft.getInstance().getMainWindow().getFramebufferHeight(), 0.05F, 10.0F));
                RenderSystem.matrixMode(5888);

                RenderSystem.translatef(0.5F, 0.5F, 0.0F);
                RenderSystem.scalef(0.5F, 0.5F, 1.0F);
                RenderSystem.translatef(17.0F / (float)it, (2.0F + (float)it / 1.5F) * ((float)(Util.milliTime() % 800000L) / 800000.0F), 0.0F);
                //RenderSystem.rotatef(((float)(it * it) * 4321.0F + (float)it * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.scalef(4.5F - (float)it / 4.0F, 4.5F - (float)it / 4.0F, 1.0F);
                RenderSystem.mulTextureByProjModelView();
                //RenderSystem.matrixMode(5888);
                RenderSystem.setupEndPortalTexGen();
            }, () -> {
                //RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                //RenderSystem.matrixMode(5888);
                RenderSystem.clearTexGen();
            });
            this.iteration = it;
        }


    }*/
}

