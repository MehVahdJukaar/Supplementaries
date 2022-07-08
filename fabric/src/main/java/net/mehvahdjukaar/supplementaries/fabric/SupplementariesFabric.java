package net.mehvahdjukaar.supplementaries.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlowerBoxBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SupplementariesFabric implements ModInitializer {

    public static final String MOD_ID = Supplementaries.MOD_ID;

    @Override
    public void onInitialize() {

        Supplementaries.commonInit();
        Supplementaries.commonRegistration();

        ServerLifecycleEvents.SERVER_STARTING.register(s -> Supplementaries.commonSetup());
    }

    //test stuff

    public static class aaa extends BlockEntity implements RenderAttachmentBlockEntity{

        @Override
        public @Nullable Object getRenderAttachmentData() {
            return null;
        }
    }

    public static class aa extends ForwardingBakedModel{

        public BakedBlackboardModel(BakedModel baseModel) {
            this.wrapped = baseModel;
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

            this.emitBlockMesh(blockView, pos, context);
        }


        protected void emitBlockMesh(BlockAndTintGetter blockView, BlockPos pos, RenderContext context) {
            var attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
            if (attachment instanceof Mesh mesh) {
                context.meshConsumer().accept(mesh);
            }
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            super.emitItemQuads(stack, randomSupplier, context);

            var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
            if (nbt != null && nbt.contains("pixels", NbtElement.BYTE_ARRAY_TYPE)) {
                var blackboard = Blackboard.fromNbt(nbt);
                context.meshConsumer().accept(blackboard.buildMesh(Direction.NORTH, blackboard.isLit() ? LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE : 0));
            }
        }


    }
    public class FlowerBoxBakedModel implements IDynamicBakedModel {
        private final BakedModel box;
        private final BlockModelShaper blockModelShaper;

        public FlowerBoxBakedModel(BakedModel box) {
            this.box = box;
            this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@javax.annotation.Nullable BlockState state, @javax.annotation.Nullable Direction side, @Nonnull RandomSource rand, @Nonnull IModelData extraData) {

            List<BakedQuad> quads = new ArrayList<>();

            //box
            try {
                quads.addAll(box.getQuads(state, side, rand, EmptyModelData.INSTANCE));
            } catch (Exception ignored) {
            }

            //mimic
            try {

                if(state != null) {
                    BlockState[] flowers = new BlockState[]{
                            extraData.getData(FlowerBoxBlockTile.FLOWER_0),
                            extraData.getData(FlowerBoxBlockTile.FLOWER_1),
                            extraData.getData(FlowerBoxBlockTile.FLOWER_2)
                    };

                    PoseStack matrixStack = new PoseStack();

                    matrixStack.translate(0.5, 0.5, 0.5);

                    float yaw = -state.getValue(FlowerBoxBlock.FACING).getOpposite().toYRot();
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
                    matrixStack.translate(-0.3125, -3 / 16f, 0);

                    if (state.getValue(FlowerBoxBlock.FLOOR)) {
                        matrixStack.translate(0, 0, -0.3125);
                    }
                    matrixStack.scale(0.625f, 0.625f, 0.625f);

                    matrixStack.translate(0.5, 0.5, 0.5);

                    matrixStack.translate(-0.5, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        BlockState flower = flowers[i];
                        if (flower != null && !flower.isAir()) {
                            this.addBlockToModel(i, quads, flower, matrixStack, side, rand);
                            if (flower.hasProperty(DoublePlantBlock.HALF)) {
                                matrixStack.translate(0, 1, 0);
                                this.addBlockToModel(i, quads, flower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), matrixStack, side, rand);
                                matrixStack.translate(0, -1, 0);
                            }
                        }

                        matrixStack.translate(0.5, 0, 0);
                    }
                }

            } catch (Exception ignored) {
            }

            //TODO: do this for hanging flower pot
            return quads;
        }

        private void addBlockToModel(int index, final List<BakedQuad> quads, BlockState state, PoseStack matrixStack, @javax.annotation.Nullable Direction side, @Nonnull RandomSource rand) {

            BakedModel model;
            //for special flowers
            //TODO: automatically scan and load models from blockstate flower folder
            ResourceLocation res = FlowerPotHandler.getSpecialFlowerModel(state.getBlock().asItem());
            if (res != null) {
                if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                    //dont render double plants
                    return;
                }
                model = blockModelShaper.getModelManager().getModel(res);
            } else {
                model = blockModelShaper.getBlockModel(state);
            }

            List<BakedQuad> mimicQuads = model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
            for (BakedQuad q : mimicQuads) {
                int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);

                TextureAtlasSprite texture = this.getParticleIcon();
                if (res == null) {
                    RendererUtil.moveVertices(v, -0.5f, -0.5f, -0.5f, texture);
                    RendererUtil.scaleVertices(v, 0.6249f, texture);
                } else {
                    RendererUtil.moveVertices(v, -0.5f, -0.5f + 3 / 16f, -0.5f, texture);
                }

                RendererUtil.transformVertices(v, matrixStack, texture);

                quads.add(new BakedQuad(v, q.getTintIndex() >= 0 ? index : q.getTintIndex(), q.getDirection(), q.getSprite(), q.isShade()));
            }
        }

}
