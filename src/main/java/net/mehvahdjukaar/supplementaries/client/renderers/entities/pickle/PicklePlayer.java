package net.mehvahdjukaar.supplementaries.client.renderers.entities.pickle;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.PicklePacket;
import net.mehvahdjukaar.supplementaries.common.utils.SpecialPlayers;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = {Dist.CLIENT})
public class PicklePlayer {
    private static PickleRenderer PICKLE_INSTANCE;
    private static JarredRenderer JARVIS_INSTANCE;
    private static boolean jarvis = false;

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PickleData.onPlayerLogOff();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PickleData.onPlayerLogin(event.getPlayer());
    }

    @SubscribeEvent
    public static void chat(ClientChatEvent event) {

        String m = event.getOriginalMessage();
        UUID id = Minecraft.getInstance().player.getGameProfile().getId();
        if (m.startsWith("/jarvis")) {
            jarvis = !jarvis;
            event.setCanceled(true);
            if (jarvis)
                Minecraft.getInstance().player.sendMessage(
                        new TextComponent("I am Jarman"), Util.NIL_UUID);
        } else if (PickleData.isDev(id)) {
            if (m.startsWith("/pickle")) {

                event.setCanceled(true);
                boolean turnOn = !PickleData.isActive(id);

                if (turnOn) {
                    Minecraft.getInstance().player.sendMessage(
                            new TextComponent("I turned myself into a pickle!"), Util.NIL_UUID);
                }

                PickleData.set(id, turnOn);
                NetworkHandler.INSTANCE.sendToServer(new PicklePacket(id, turnOn));
            }
        }
    }

    public static void createRenderInstance(EntityRendererProvider.Context context) {
        PICKLE_INSTANCE = new PickleRenderer(context);
        JARVIS_INSTANCE = new JarredRenderer(context);
    }


    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        UUID id = event.getPlayer().getGameProfile().getId();

        if (PickleData.isActiveAndTick(id, event.getRenderer()) )  {
            event.setCanceled(true);

            float rot = Mth.rotLerp(event.getPlayer().yRotO, event.getPlayer().getYRot(), event.getPartialTick());
            PICKLE_INSTANCE.render((AbstractClientPlayer) event.getPlayer(), rot, event.getPartialTick(),
                    event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        } else if (jarvis && id.equals(Minecraft.getInstance().player.getUUID()) && JARVIS_INSTANCE != null) {
            event.setCanceled(true);

            float rot = Mth.rotLerp(event.getPlayer().yRotO, event.getPlayer().getYRot(), event.getPartialTick());
            JARVIS_INSTANCE.render((AbstractClientPlayer) event.getPlayer(), rot, event.getPartialTick(),
                    event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }

    }


    //server and client side. might move into data
    public static class PickleData {

        public static final Map<UUID, PickleValues> PICKLE_PLAYERS = new HashMap<>();

        static {
            for (UUID id : SpecialPlayers.DEVS) PICKLE_PLAYERS.put(id, new PickleValues());
        }

        //reset
        public static void onPlayerLogOff() {
            for (PickleValues val : PICKLE_PLAYERS.values()) {
                val.reset();
            }
        }

        public static void onPlayerLogin(Player player) {
            for (UUID id : PICKLE_PLAYERS.keySet()) {
                boolean on = PICKLE_PLAYERS.get(id).isOn();
                if (on) {
                    //to client
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                            new PicklePacket(id, on));
                }
            }
        }

        public static boolean isDev(UUID id) {
            return SpecialPlayers.DEVS.contains(id);
        }

        public static void set(UUID id, boolean on) {
            PICKLE_PLAYERS.getOrDefault(id, DEF).toggle(on);
        }

        public static boolean isActiveAndTick(UUID id, PlayerRenderer renderer) {
            return PICKLE_PLAYERS.getOrDefault(id, DEF).isOnAndTick(renderer);
        }

        public static boolean isActive(UUID id) {
            return PICKLE_PLAYERS.getOrDefault(id, DEF).isOn();
        }

        private static final PickleValues DEF = new PickleValues();

        public static class PickleValues {
            private State state = State.OFF;
            private float oldShadowSize = 1;

            public void toggle(boolean on) {
                if (on) this.state = State.FIRST_ON;
                else this.state = State.FIRST_OFF;
            }

            public void reset() {
                this.state = State.OFF;
            }

            public boolean isOnAndTick(PlayerRenderer renderer) {
                switch (this.state) {
                    case ON:
                        return true;
                    default:
                    case OFF:
                        return false;
                    case FIRST_ON:
                        this.oldShadowSize = renderer.shadowRadius;
                        renderer.shadowRadius = 0;
                        this.state = State.ON;
                        return true;
                    case FIRST_OFF:
                        renderer.shadowRadius = this.oldShadowSize;
                        this.state = State.OFF;
                        return true;
                }
            }

            public boolean isOn() {
                return this.state == State.ON || this.state == State.FIRST_ON;
            }

            private enum State {
                ON, OFF, FIRST_ON, FIRST_OFF;
            }
        }
    }
}