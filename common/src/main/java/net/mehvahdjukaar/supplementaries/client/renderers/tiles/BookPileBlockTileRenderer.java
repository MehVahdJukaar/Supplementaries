package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileHorizontalBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile.BooksList;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile.VisualBook;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class BookPileBlockTileRenderer implements BlockEntityRenderer<BookPileBlockTile> {

    private static ModelBlockRenderer renderer;

    public BookPileBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
    }


    @Override
    public boolean shouldRender(BookPileBlockTile blockEntity, Vec3 cameraPos) {
        return ClientConfigs.Tweaks.BOOK_GLINT.get();
    }

    @Override
    public void render(BookPileBlockTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int light,
                       int overlay) {
        BlockState state = tile.getBlockState();
        renderBookPile(tile.horizontal, tile.booksVisuals, matrixStack, v -> v.getBuilder(bufferIn), light, overlay, state);
    }

    public static void renderBookPile(boolean horizontal, BooksList books, PoseStack matrixStack,
                                      Function<VisualBook, VertexConsumer> bufferIn,
                                      int light, int overlay, BlockState state) {

        if (horizontal) {
            renderHorizontal(books, state, matrixStack, bufferIn, light, overlay);
        } else {
            renderVertical(books, state, matrixStack, bufferIn, light, overlay);
        }
    }

    private static void renderHorizontal(BooksList visualBooks, BlockState state, PoseStack poseStack, Function<VisualBook, VertexConsumer> buffer, int light, int overlay) {
        int books = Math.min(state.getValue(BookPileBlock.BOOKS), visualBooks.size());

        Direction dir = state.getValue(BookPileHorizontalBlock.FACING);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(RotHlpr.rot(dir));
        poseStack.translate(-0.5, -0.5 - 3 / 16f, -0.5);

        float angle = (11.25f) * Mth.DEG_TO_RAD;
        switch (books) {
            case 4 -> {
                poseStack.translate(-6 / 16f, 0, 0);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(0));
                poseStack.translate(4 / 16f, 0, -1 / 16f);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(1));
                poseStack.translate(4 / 16f, 0, 1 / 16f);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(2));
                poseStack.translate(4 / 16f, 0, -1 / 16f);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(3));
            }
            case 3 -> {
                poseStack.translate(-5 / 16f, 0, 0);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(0));
                poseStack.translate(4 / 16f, 0, -1 / 16f);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(1));
                poseStack.translate(5 / 16f, 0, 1 / 16f);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(2), 0, angle);
            }
            case 2 -> {
                poseStack.translate(-3 / 16f, 0, 0);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(0));
                poseStack.translate(5 / 16f, 0, 1 / 16f);
                renderBook(poseStack, buffer, light, overlay, visualBooks.get(1), 0, angle);
            }
            case 1 -> renderBook(poseStack, buffer, light, overlay, visualBooks.get(0));
            default -> {
            }
        }
    }

    private static void renderVertical(BooksList booksList, BlockState state, PoseStack matrixStack, Function<VisualBook, VertexConsumer> builder, int light, int overlay) {

        int maxBooks = Math.min(state.getValue(BookPileBlock.BOOKS), booksList.size());
        matrixStack.translate(0, -6 / 16f, 0);

        float zRot = -(float) (Math.PI / 2f);

        for (int i = 0; i < maxBooks; i++) {
            VisualBook b = booksList.get(i);

            renderBook(matrixStack, builder, light, overlay, b, b.getAngle(), zRot);

            matrixStack.translate(0, 4 / 16f, 0);
        }
    }

    private static void renderBook(PoseStack poseStack, Function<VisualBook, VertexConsumer> vertexBuilder,
                                   int light, int overlay, VisualBook b) {
        renderBook(poseStack, vertexBuilder, light, overlay, b, 0, 0);
    }

    private static void renderBook(PoseStack poseStack, Function<VisualBook, VertexConsumer> vertexBuilder,
                                   int light, int overlay, VisualBook b, float xRot, float zRot) {
        VertexConsumer builder = vertexBuilder.apply(b);
        if (builder == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (zRot != 0) poseStack.mulPose(Axis.ZP.rotation(zRot));
        if (xRot != 0) poseStack.mulPose(Axis.XP.rotation(xRot));
        poseStack.translate(-0.5, -0.5 + 3 / 16f, -0.5);

        //TODO: swap with java model for correct shading. same for wall lanterns and block animation a good place
        BakedModel model = ClientHelper.getModel(Minecraft.getInstance().getModelManager(), b.getType().modelPath());
        if(model != null) {
            renderer.renderModel(poseStack.last(),
                    builder,
                    null,
                    model,
                    1.0F, 1.0F, 1.0F,
                    light, overlay);
        }else{
            Supplementaries.error();
        }
        poseStack.popPose();
    }

}
