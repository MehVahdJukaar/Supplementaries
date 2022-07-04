package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.client.renderUtils.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileHorizontalBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile.VisualBook;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BookPileBlockTileRenderer implements BlockEntityRenderer<BookPileBlockTile> {

    private final ModelPart book;
    private final ModelPart lock;

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition book = root.addOrReplaceChild("book", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 10.0F, 7.0F)
                        .texOffs(28, 6).addBox(1.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F)
                        .texOffs(23, 6).addBox(-2.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F),
                PartPose.ZERO);

        book.addOrReplaceChild("lock", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    public BookPileBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.BOOK_MODEL);
        this.book = model.getChild("book");
        this.lock = this.book.getChild("lock");
    }

    @Override
    public void render(BookPileBlockTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int light,
                       int overlay) {

        BlockState state = tile.getBlockState();

        matrixStack.translate(0.5, 0.5, 0.5);

        if (tile.horizontal) {
            this.renderHorizontal(tile.books, state, matrixStack, bufferIn, light, overlay);
        } else {
            this.renderVertical(tile, state, matrixStack, bufferIn, light, overlay);
        }

    }

    private void renderHorizontal(List<VisualBook> visualBooks, BlockState state, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        int books = Math.min(state.getValue(BookPileBlock.BOOKS), visualBooks.size());

        Direction dir = state.getValue(BookPileHorizontalBlock.FACING);
        matrixStack.mulPose(RotHlpr.rot(dir));
        matrixStack.mulPose(RotHlpr.X90);

        matrixStack.translate(0, 3 / 16f, 0);

        float angle = (float) ((-11.25f) * Math.PI / 180f);
        switch (books) {
            default -> {
            }
            case 4 -> {
                matrixStack.translate(-6 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(1));
                matrixStack.translate(4 / 16f, 0, 1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(2));
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(3));
            }
            case 3 -> {
                matrixStack.translate(-5 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(1));
                matrixStack.translate(5 / 16f, 0, 1 / 16f);
                book.zRot = angle;
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(2));
                book.zRot = 0;
            }
            case 2 -> {
                matrixStack.translate(-3 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
                matrixStack.translate(5 / 16f, 0, 1 / 16f);
                book.zRot = angle;
                this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(1));
                book.zRot = 0;
            }
            case 1 -> this.renderBook(matrixStack, buffer, light, overlay, visualBooks.get(0));
        }
    }

    private void renderVertical(BookPileBlockTile tile, BlockState state, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

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

    private void renderBook(PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, VisualBook b) {
        VertexConsumer builder = b.getBuilder(buffer);
        this.lock.visible = b.isEnchanted();
        this.book.render(matrixStack, builder, light, overlay);
    }

}
