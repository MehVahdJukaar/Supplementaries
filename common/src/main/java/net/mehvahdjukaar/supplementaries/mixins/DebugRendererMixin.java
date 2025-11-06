package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    @Final
    public GoalSelectorDebugRenderer goalSelectorRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;

    @Shadow
    @Final
    public PathfindingRenderer pathfindingRenderer;

    @Inject(method = "render", at = @At("TAIL"))
    public void supp$renderVanillaDebug(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {

        if (ClientConfigs.General.DEBUG_RENDERS.get()) {
            this.goalSelectorRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            this.neighborsUpdateRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            this.pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        }
    }
}
