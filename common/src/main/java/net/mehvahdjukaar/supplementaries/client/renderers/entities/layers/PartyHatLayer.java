package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class PartyHatLayer<E extends LivingEntity, M extends EntityModel<E>> extends RenderLayer<E, M> {

    protected final ModelPart string;
    protected final ModelPart parentHead;

    public PartyHatLayer(RenderLayerParent<E, M> renderer, EntityModelSet entityModels) {
        super(renderer);
        this.string = entityModels.bakeLayer(ClientRegistry.PARTY_CREEPER_MODEL);
        this.parentHead = getParentHead(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, E entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isInvisible() && hasHat(entity)) {
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCullZOffset(ModTextures.PARTY_CREEPER));
            poseStack.pushPose();

            this.string.copyFrom(this.parentHead);
            this.string.render(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
            this.renderExtra(poseStack, buffer, packedLight, entity);
            poseStack.popPose();
        }
    }

    protected void renderExtra(PoseStack poseStack, MultiBufferSource buffer, int packedLight, E entity) {
    }

    protected abstract ModelPart getParentHead(RenderLayerParent<E, M> renderer);

    protected abstract boolean hasHat(E livingEntity);

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                        CubeDeformation.NONE), PartPose.ZERO);
        if(PlatHelper.getPlatform().isFabric()) {
            partDefinition.addOrReplaceChild("hat_1", CubeListBuilder.create()
                            .texOffs(0, 32 - 9)
                            .addBox(-4.0F, -16.0F, 0.0F, 8.0F, 9.0F, 0.0F,
                                    new CubeDeformation(4 * Mth.sqrt(2) - 4, 0, 0)),
                    PartPose.rotation(0, Mth.PI / 4, 0));
            partDefinition.addOrReplaceChild("hat_2", CubeListBuilder.create()
                            .texOffs(0, 32 - 9)
                            .addBox(-4.0F, -16.0F, 0.0F, 8.0F, 9.0F, 0.0F,
                                    new CubeDeformation(4 * Mth.sqrt(2) - 4, 0, 0)),
                    PartPose.rotation(0, -Mth.PI / 4, 0));
        }
        return LayerDefinition.create(meshDefinition, 32, 32);
    }


    public static class Generic<P extends LivingEntity> extends PartyHatLayer<P, HumanoidModel<P>> {

        public Generic(RenderLayerParent<P, HumanoidModel<P>> renderer, EntityModelSet entityModels) {
            super(renderer, entityModels);
        }

        @Override
        protected boolean hasHat(P livingEntity) {
            return livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModRegistry.CONFETTI_POPPER.get();
        }

        @Override
        protected ModelPart getParentHead(RenderLayerParent<P, HumanoidModel<P>> renderer) {
            return this.getParentModel().head;
        }

    }

    public static class Creeper<C extends net.minecraft.world.entity.monster.Creeper & IPartyCreeper> extends PartyHatLayer<C, CreeperModel<C>> {

        private final ItemInHandRenderer itemRenderer;
        private final ItemStack stack;
        private final boolean rendersItem;

        public Creeper(RenderLayerParent<C, CreeperModel<C>> renderer, EntityModelSet entityModels, ItemInHandRenderer itemRenderer) {
            super(renderer, entityModels);
            this.itemRenderer = itemRenderer;
            this.stack = ModRegistry.CONFETTI_POPPER.get().getDefaultInstance();
            this.rendersItem = PlatHelper.getPlatform().isForge();
        }

        @Override
        protected boolean hasHat(C livingEntity) {
            return livingEntity.supplementaries$isFestive();
        }

        @Override
        protected ModelPart getParentHead(RenderLayerParent<C, CreeperModel<C>> renderer) {
            return getParentModel().root().getChild("head");
        }

        @Override
        protected void renderExtra(PoseStack poseStack, MultiBufferSource buffer, int packedLight, C entity) {
            if (this.rendersItem) {
                CustomHeadLayer.translateToHead(poseStack, false);
                itemRenderer.renderItem(entity, stack, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight);
            }
        }
    }
}
