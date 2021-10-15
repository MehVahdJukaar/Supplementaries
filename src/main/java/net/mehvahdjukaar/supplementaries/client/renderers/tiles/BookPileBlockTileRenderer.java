package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileHorizontalBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.Textures.BookColor;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.enchantedbooks.EnchantedBookRedesignRenderer;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BookPileBlockTileRenderer extends BlockEntityRenderer<BookPileBlockTile> {

    private final ModelPart book = new ModelPart(32, 32, 0, 0);
    private final ModelPart lock = new ModelPart(32, 32, 0, 0);
    private final boolean vertical;

    public BookPileBlockTileRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        this(rendererDispatcherIn, false);
    }

    public BookPileBlockTileRenderer(BlockEntityRenderDispatcher rendererDispatcherIn, boolean vertical) {
        super(rendererDispatcherIn);
        this.vertical = vertical;

        book.texOffs(0, 0).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 10.0F, 7.0F, 0.0F, false);
        book.texOffs(28, 6).addBox(1.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);
        book.texOffs(23, 6).addBox(-2.0F, -5.0F, 3.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);


        lock.texOffs(0, 0).addBox(-1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);
        book.addChild(lock);
    }

    @Override
    public void render(BookPileBlockTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int light,
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

    private void renderHorizontal(BlockState state, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, Random random) {
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

    private void renderVertical(ItemDisplayTile inventory, BlockState state, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, Random random) {

        int books = state.getValue(BookPileBlock.BOOKS);

        //TODO: add support for quark

        boolean glint = ClientConfigs.cached.BOOK_GLINT;
        boolean coloredGlint = CompatHandler.enchantedbookredesign;

        VertexConsumer builder;


        matrixStack.translate(0, -6 / 16f, 0);
        book.zRot = (float) (Math.PI / 2f);

        for (int i = 0; i < books; i++) {
            book.xRot = (float) (random.nextInt(32) * Math.PI / 16);

            //gets new builder

            builder = this.getBuilderWithFoil(inventory.getItem(i), buffer, glint, coloredGlint);

            book.render(matrixStack, builder, light, overlay);

            matrixStack.translate(0, 4 / 16f, 0);

        }
        book.xRot = 0;
        book.zRot = 0;
    }


    private VertexConsumer getBuilderWithFoil(ItemStack stack, MultiBufferSource buffer, boolean glint, boolean color) {

        Material mat = (CompatHandler.quark && QuarkPlugin.isTome(stack.getItem())) ? Materials.BOOK_TOME_MATERIAL : Materials.BOOK_ENCHANTED_MATERIAL;
        if(glint) {
            VertexConsumer foilBuilder = null;
            if (color) {
                foilBuilder = EnchantedBookRedesignRenderer.getColoredFoil(stack, buffer);
            }
            if (foilBuilder == null) {
                foilBuilder = buffer.getBuffer(RenderType.entityGlint());
            }
            return VertexMultiConsumer.create(foilBuilder, mat.buffer(buffer, RenderType::entitySolid));
        }
        else return  mat.buffer(buffer, RenderType::entitySolid);
    }

    private void renderBook(PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, Random random, List<BookColor> colors) {
        int ind = random.nextInt(colors.size());
        BookColor color = colors.remove(ind);
        VertexConsumer builder = Materials.BOOK_MATERIALS.get(color).buffer(buffer, RenderType::entitySolid);

        book.render(matrixStack, builder, light, overlay);
    }

}
