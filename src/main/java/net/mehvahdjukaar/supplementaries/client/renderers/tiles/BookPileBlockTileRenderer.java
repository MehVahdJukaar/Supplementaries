package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileHorizontalBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.Textures.BookColor;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.enchantedbooks.EnchantedBookRedesignRenderer;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BookPileBlockTileRenderer extends TileEntityRenderer<BookPileBlockTile> {

    private final ModelRenderer book = new ModelRenderer(32, 32, 0, 0);
    private final ModelRenderer lock = new ModelRenderer(32, 32, 0, 0);
    private final boolean vertical;

    public BookPileBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        this(rendererDispatcherIn, false);
    }

    public BookPileBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn, boolean vertical) {
        super(rendererDispatcherIn);
        this.vertical = vertical;

        book.texOffs(0, 0).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 10.0F, 7.0F, 0.0F, false);
        book.texOffs(28, 6).addBox(1.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);
        book.texOffs(23, 6).addBox(-2.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);


        lock.texOffs(0, 0).addBox(-1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);
        book.addChild(lock);
    }

    @Override
    public void render(BookPileBlockTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int light,
                       int overlay) {
        long r = tile.getBlockPos().asLong();
        Random rand = new Random(r);

        BlockState state = tile.getBlockState();

        matrixStack.translate(0.5, 0.5, 0.5);

        if (tile.horizontal) {
            this.renderHorizontal(state, matrixStack, bufferIn, light, overlay, rand);
        } else {
            this.renderVertical(tile, state, matrixStack, bufferIn, light, overlay, rand);
        }

    }

    private void renderHorizontal(BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, Random random) {
        int books = state.getValue(BookPileBlock.BOOKS);

        Direction dir = state.getValue(BookPileHorizontalBlock.FACING);
        matrixStack.mulPose(Const.rot(dir));
        matrixStack.mulPose(Const.X90);

        matrixStack.translate(0, 3 / 16f, 0);

        List<BookColor> colors = new ArrayList<>(Arrays.asList(BookColor.values()));
        lock.visible = false;
        float angle = (float) ((-11.25f) * Math.PI / 180f);
        switch (books) {
            default:
            case 4:
                matrixStack.translate(-6 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                matrixStack.translate(4 / 16f, 0, 1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                break;
            case 3:
                matrixStack.translate(-5 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                matrixStack.translate(4 / 16f, 0, -1 / 16f);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                matrixStack.translate(5 / 16f, 0, 1 / 16f);
                book.zRot = angle;
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                book.zRot = 0;
                break;
            case 2:
                matrixStack.translate(-3 / 16f, 0, 0);
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                matrixStack.translate(5 / 16f, 0, 1 / 16f);
                book.zRot = angle;
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                book.zRot = 0;
                break;
            case 1:
                this.renderBook(matrixStack, buffer, light, overlay, random, colors);
                break;
        }
        lock.visible = true;
    }

    private void renderVertical(ItemDisplayTile inventory, BlockState state, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, Random random) {

        int books = state.getValue(BookPileBlock.BOOKS);

        //TODO: add support for quark

        boolean glint = ClientConfigs.cached.BOOK_GLINT;
        boolean coloredGlint = CompatHandler.enchantedbookredesign;

        IVertexBuilder builder;
        if(!glint){
            builder = Materials.ENCHANTED_BOOK_MATERIAL.buffer(buffer, RenderType::entitySolid);
        }
        else{
            builder = this.getBuilderWithFoil(inventory.getItem(0), buffer, coloredGlint);
        }

        matrixStack.translate(0, -6 / 16f, 0);
        book.zRot = (float) (Math.PI / 2f);

        for (int i = 0; i < books; i++) {
            book.xRot = (float) (random.nextInt(16) * Math.PI / 8);

            //gets new builder
            if(coloredGlint && glint && i != 0){
                builder = this.getBuilderWithFoil(inventory.getItem(i), buffer, coloredGlint);
            }

            book.render(matrixStack, builder, light, overlay);

            matrixStack.translate(0, 4 / 16f, 0);

        }
        book.xRot = 0;
        book.zRot = 0;
    }


    private IVertexBuilder getBuilderWithFoil(ItemStack stack, IRenderTypeBuffer buffer, boolean color) {
        IVertexBuilder foilBuilder = null;
        if (color) {
            foilBuilder = EnchantedBookRedesignRenderer.getColoredFoil(stack, buffer);
        }
        if (foilBuilder == null) {
            foilBuilder = buffer.getBuffer(RenderType.entityGlint());
        }
        return VertexBuilderUtils.create(foilBuilder, Materials.ENCHANTED_BOOK_MATERIAL.buffer(buffer, RenderType::entitySolid));
    }

    private void renderBook(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, Random random, List<BookColor> colors) {
        int ind = random.nextInt(colors.size());
        BookColor color = colors.remove(ind);
        IVertexBuilder builder = Materials.BOOK_MATERIALS.get(color).buffer(buffer, RenderType::entitySolid);

        book.render(matrixStack, builder, light, overlay);
    }

}
