package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LeashKnotRenderer.class)
public abstract class LeashKnotRendererMixin {

    @Unique
    private ModelPart supp$ropeKnot;
    @Unique
    private ModelPart supp$smallRopeKnot;

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/LeashKnotModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void supp$renderOnRope(LeashKnotModel<?> model, PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                                   Operation<Void> original, @Local(argsOnly = true) LeashFenceKnotEntity entity) {
        BlockState state = entity.level().getBlockState(entity.getPos());
        if (state.getBlock() == ModRegistry.ROPE.get()) {
            //lazy. otherwise cascading errors due to neo might blame this
            if (supp$ropeKnot == null) {
                EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
                supp$ropeKnot = modelSet.bakeLayer(ClientRegistry.ROPE_LEASH_KNOT_MODEL);
                supp$smallRopeKnot = modelSet.bakeLayer(ClientRegistry.SMALL_ROPE_LEASH_KNOT_MODEL);
            }
            ModelPart knot = state.getValue(RopeBlock.KNOT) ? supp$ropeKnot : supp$smallRopeKnot;
            knot.render(poseStack, buffer, light, overlay);
        } else original.call(model, poseStack, buffer, light, overlay);
    }
}
