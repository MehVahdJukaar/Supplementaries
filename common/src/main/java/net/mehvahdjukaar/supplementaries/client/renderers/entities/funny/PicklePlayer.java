package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.PicklePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.UUID;

//shh, go away don't look here
public class PicklePlayer {

    private static boolean jarvis = false;

    public static boolean onChatEvent(String m) {
        UUID id = Minecraft.getInstance().player.getGameProfile().getId();
        if (m.startsWith("/jarman")) {
            jarvis = !jarvis;
            if (jarvis) {
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("I am Jarman"), true);
            }
            return true;
        }
        boolean jar = m.startsWith("/jar");
        if (PickleData.isDev(id, jar)) {
            boolean pick = m.startsWith("/pickle");
            if (pick || jar) {
                boolean turnOn = !PickleData.isActive(id);

                if (turnOn && pick) {
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("I turned myself into a pickle!"), true);
                }
                PickleData.set(id, turnOn, jar);
                NetworkHelper.sendToServer(new PicklePacket(id, turnOn, jar));
                return true;
            }
        }
        return false;
    }

    //true when we drew the player ourselves
    public static boolean renderInstead(PlayerRenderer renderer, AbstractClientPlayer player, float partialTick,
                                        PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        UUID id = player.getGameProfile().getId();
        float rot = Mth.rotLerp(player.yRotO, player.getYRot(), partialTick);

        if (PickleData.isActiveAndTick(id, renderer) && PickleRenderer.INSTANCE != null) {
            PickleRenderer.INSTANCE.render(player, rot, partialTick, poseStack, bufferSource, packedLight);
            return true;
        }
        //only for local player
        else if (jarvis && id.equals(Minecraft.getInstance().player.getUUID()) && JarredRenderer.INSTANCE != null) {
            JarredRenderer.INSTANCE.render(player, rot, partialTick, poseStack, bufferSource, packedLight);
            return true;
        }
        return false;
    }
}
