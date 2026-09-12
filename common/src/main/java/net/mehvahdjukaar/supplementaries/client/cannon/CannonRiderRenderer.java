package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.player.Player;

public class CannonRiderRenderer {

    private static final float HEAD_PIVOT_HEIGHT = 1.5f + 0.25f;

    private static boolean renderingFromCannon = false; //TODO: check

    public static boolean isRenderingFromCannon() {
        return renderingFromCannon;
    }

    public static void renderRider(CannonBlockTile cannon, float partialTick, PoseStack poseStack,
                                   MultiBufferSource bufferSource, int packedLight) {
        Player rider = cannon.getRider();
        if (rider == null || rider.isInvisible()) return;
        EntityRenderer<? super Player> renderer = Minecraft.getInstance()
                .getEntityRenderDispatcher().getRenderer(rider);

        poseStack.pushPose();
        poseStack.translate(0, -1 / 16, 6.5f / 16f);
        //cannon space has y pointing down (block base rotation), flip it back or the head renders upside down
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -HEAD_PIVOT_HEIGHT, 0);

        //head is drawn looking down the barrel no matter where the player looks
        float headYaw = rider.yHeadRot;
        float headYawO = rider.yHeadRotO;
        float pitch = rider.getXRot();
        float pitchO = rider.xRotO;
        rider.yHeadRot = rider.yBodyRot;
        rider.yHeadRotO = rider.yBodyRotO;
        rider.setXRot(0);
        rider.xRotO = 0;

        renderingFromCannon = true;
        try {
            renderer.render(rider, 0, partialTick, poseStack, bufferSource, packedLight);
        } finally {
            renderingFromCannon = false;
            rider.yHeadRot = headYaw;
            rider.yHeadRotO = headYawO;
            rider.setXRot(pitch);
            rider.xRotO = pitchO;
        }
        poseStack.popPose();
    }
}
