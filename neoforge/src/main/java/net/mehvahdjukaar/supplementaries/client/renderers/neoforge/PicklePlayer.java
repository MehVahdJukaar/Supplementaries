package net.mehvahdjukaar.supplementaries.client.renderers.neoforge;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PickleData;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PickleRenderer;
import net.mehvahdjukaar.supplementaries.common.network.PicklePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

//shh, go away don't look here
@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = {Dist.CLIENT})
public class PicklePlayer {

    private static boolean jarvis = false;

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PickleData.onPlayerLogOff();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PickleData.onPlayerLogin(event.getEntity());
    }

    //event doesn't fire for "/" anymore....
    //@SubscribeEvent
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


    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        UUID id = player.getGameProfile().getId();

        if (PickleData.isActiveAndTick(id, event.getRenderer()) && PickleRenderer.INSTANCE != null) {
            event.setCanceled(true);

            float rot = Mth.rotLerp(player.yRotO, player.getYRot(), event.getPartialTick());
            PickleRenderer.INSTANCE.render((AbstractClientPlayer) player, rot, event.getPartialTick(),
                    event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
        //only for local player
        else if (jarvis && id.equals(Minecraft.getInstance().player.getUUID()) && JarredRenderer.INSTANCE != null) {
            event.setCanceled(true);

            float rot = Mth.rotLerp(player.yRotO, player.getYRot(), event.getPartialTick());
            JarredRenderer.INSTANCE.render((AbstractClientPlayer) player, rot, event.getPartialTick(),
                    event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }

    }


}
