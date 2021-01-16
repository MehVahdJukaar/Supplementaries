package net.mehvahdjukaar.supplementaries.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;

@OnlyIn(Dist.CLIENT)
public class HourGlassBlockTileRenderer extends TileEntityRenderer<HourGlassBlockTile> {
    public HourGlassBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(HourGlassBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if(tile.sandType.isEmpty())return;
        ResourceLocation texture = tile.sandType.texture;
        if(tile.sandType.isSand()){
            ItemStack stack = tile.getStackInSlot(0);
            Item i = stack.getItem();
            ResourceLocation reg = i.getRegistryName();
            //BlockState state = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
            //ModelLoader.instance().getModelOrMissing(new ResourceLocation()).
            //IBakedModel baked = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state)
            //ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            //IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);
            //IBakedModel baked2 = Minecraft.getInstance().getBlockRendererDispatcher().;
            //IUnbakedModel unbaked = ModelLoader.instance().getModelOrMissing(new ResourceLocation(reg.getNamespace(),"block/"+reg.getPath()));
            //unbaked.getTextures()
            //TODO: find more general solution
            texture = new ResourceLocation(reg.getNamespace(),"block/"+reg.getPath());
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);

        if(sprite instanceof MissingTextureSprite)sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(tile.sandType.texture);

        int color = 0xffffff;
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
        float h = MathHelper.lerp(partialTicks, tile.prevProgress, tile.progress);

        matrixStackIn.push();

        matrixStackIn.translate(0.5,0.5,0.5);
        matrixStackIn.rotate(tile.getBlockState().get(HourGlassBlock.FACING).getRotation());
        Direction dir = tile.getBlockState().get(HourGlassBlock.FACING);
        Quaternion q = dir.getRotation();
        q.conjugate();



        if(h!=0) {
            matrixStackIn.push();
            matrixStackIn.translate(0,-0.25,0);
            matrixStackIn.rotate(q);
            matrixStackIn.translate(0,-0.125,0);
            float h1 = h * 0.25f;
            RendererUtil.addCube(builder, matrixStackIn, 0.25f, h1, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    true, true, true);
            if(dir==Direction.DOWN) {
                matrixStackIn.translate(0, -h1 - 0.25f, 0);
                RendererUtil.addCube(builder, matrixStackIn, 0.0625f, h1 + 0.25f, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                        true, true, false);
            }
            matrixStackIn.pop();
        }
        if(h!=1) {
            matrixStackIn.push();
            matrixStackIn.translate(0,0.25,0);
            matrixStackIn.rotate(q);
            matrixStackIn.translate(0,-0.125,0);
            float h2 = (1 - h) * 0.25f;
            RendererUtil.addCube(builder, matrixStackIn, 0.25f, h2 , sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    true, true, true);
            if(dir==Direction.UP) {
                matrixStackIn.translate(0, -h2 -0.25, 0);
                RendererUtil.addCube(builder, matrixStackIn, 0.0625f, h2 + 0.25f, sprite, combinedLightIn, color, 1f, combinedOverlayIn, true,
                        true, true, false);
            }
            matrixStackIn.pop();
        }
        matrixStackIn.pop();
    }
}