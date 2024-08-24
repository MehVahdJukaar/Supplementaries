package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperPartyHatLayer<E extends Creeper & IPartyCreeper, M extends CreeperModel<E>> extends RenderLayer<E, M> {

    private final ModelPart hat;
    private final ModelPart parentHead;

    public CreeperPartyHatLayer(RenderLayerParent<E, M> renderer, EntityModelSet entityModels) {
        super(renderer);
        this.hat = entityModels.bakeLayer(ClientRegistry.PARTY_CREEPER_MODEL);
        this.parentHead = renderer.getModel().root().getChild("head");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, E livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!livingEntity.isInvisible() && livingEntity.supplementaries$isFestive()) {
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(ModTextures.PARTY_CREEPER));
            this.hat.copyFrom(parentHead);
            this.hat.render(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F));
        }
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        var head = partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                        CubeDeformation.NONE), PartPose.ZERO);
        head.addOrReplaceChild("hat_1", CubeListBuilder.create()
                        .texOffs(0, 32 - 9)
                        .addBox(-4.0F, -16.0F, 0.0F, 8.0F, 9.0F, 0.0F,
                                new CubeDeformation(4 * Mth.sqrt(2) - 4, 0, 0)),
                PartPose.rotation(0, Mth.PI / 4, 0));
        head.addOrReplaceChild("hat_2", CubeListBuilder.create()
                        .texOffs(0, 32 - 9)
                        .addBox(-4.0F, -16.0F, 0.0F, 8.0F, 9.0F, 0.0F,
                                new CubeDeformation(4 * Mth.sqrt(2) - 4, 0, 0)),
                PartPose.rotation(0, -Mth.PI / 4, 0));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

}
