package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileHorizontalBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile.VisualBook;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

import java.util.List;

public class BookPileBlockTileRenderer extends TileEntityRenderer<BookPileBlockTile> {

    private final ModelRenderer book = new ModelRenderer(32, 32, 0, 0);
    private final ModelRenderer lock = new ModelRenderer(32, 32, 0, 0);

    public BookPileBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        book.texOffs(0, 0).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 10.0F, 7.0F, 0.0F, false);
        book.texOffs(28, 6).addBox(1.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);
        book.texOffs(23, 6).addBox(-2.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);


        lock.texOffs(0, 0).addBox(-1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);
        book.addChild(lock);
    }

    @Override
    public void render(BookPileBlockTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int light,
                       int overlay) {

        BlockState state = tile.getBlockState();

        matrixStack.translate(0.5, 0.5, 0.5);

        if (tile.horizontal) {
            this.renderHorizontal(tile.books, state, matrixStack, bufferIn, light, overlay);
        } else {
            this.renderVertical(tile, state, matrixStack, bufferIn, light, overlay);
        }

    }

    private void renderHorizontal(List<VisualBook> visualBooks, BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        int books = Math.min(state.getValue(BookPileBlock.BOOKS), visualBooks.size());

        Direction dir = state.getValue(BookPileHorizontalBlock.FACING);
        matrixStack.mulPose(Const.rot(dir));
        matrixStack.mulPose(Const.X90);

        matrixStack.translate(0, 3 / 16f, 0);

        float angle = (float) ((-11.25f) * Math.PI / 180f);
        switch (books) {
            default:
                break;
            case 4:
                matrixStack.translate(-6 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(1));
                matrixStack.translate(4 / 16f, 0, 1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(2));
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(3));
                break;
            case 3:
                matrixStack.translate(-5 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(1));
                matrixStack.translate(5 / 16f, 0, 1 / 16f);
                book.zRot = angle;
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(2));
                book.zRot = 0;
                break;
            case 2:
                matrixStack.translate(-3 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                matrixStack.translate(5 / 16f, 0, 1 / 16f);
                book.zRot = angle;
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(1));
                book.zRot = 0;
                break;
            case 1:
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                break;
        }
    }

    private void renderVertical(BookPileBlockTile tile, BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {

        int books = Math.min(state.getValue(BookPileBlock.BOOKS), tile.books.size());

        matrixStack.translate(0, -6 / 16f, 0);
        book.zRot = (float) (Math.PI / 2f);

        for (int i = 0; i < books; i++) {
            VisualBook b = tile.books.get(i);
            book.xRot = b.getAngle();

            this.renderBook(matrixStack, buffer, light, overlay, b);

            matrixStack.translate(0, 4 / 16f, 0);
        }
        book.xRot = 0;
        book.zRot = 0;
    }

    private void renderBook(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, VisualBook b) {
        IVertexBuilder builder = b.getBuilder(buffer);
        this.lock.visible = b.isEnchanted();
        this.book.render(matrixStack, builder, light, overlay);
    }

}
