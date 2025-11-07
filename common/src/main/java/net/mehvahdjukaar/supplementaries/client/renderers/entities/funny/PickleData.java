package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.PicklePacket;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//server and client side. might move into data
public class PickleData {

    private static final UUID ME = UUID.fromString("898b3a39-e486-405c-a873-d6b472dc3ba2");
    protected static final Map<UUID, PickleValues> PICKLE_PLAYERS = new HashMap<>();

    static {
        for (UUID id : Credits.INSTANCE.getDevs()) {
            PickleValues value = new PickleValues();
            PICKLE_PLAYERS.put(id, value);
            if (ME.equals(id)) value.toggle(true, true);
        }
    }

    //reset
    public static void onPlayerLogOff() {
        for (PickleValues val : PICKLE_PLAYERS.values()) {
            val.reset();
        }
    }

    public static void onPlayerLogin(Player player) {
        for (var e : PICKLE_PLAYERS.entrySet()) {
            boolean on = e.getValue().isOn();
            var id = e.getKey();
            if (on) {
                //to client
                if (player instanceof ServerPlayer sp) {
                    NetworkHelper.sendToClientPlayer(sp, new PicklePacket(id, on, e.getValue().isJar));
                }
            }
        }
    }

    public static boolean isDev(UUID id, boolean isJar) {
        return PICKLE_PLAYERS.containsKey(id); //isJar ? id.equals(ME) :
    }

    public static void set(UUID id, boolean on, boolean isJar) {
        PICKLE_PLAYERS.getOrDefault(id, DEF).toggle(on, isJar);
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
        private boolean isJar = false;
        private float oldShadowSize = 1;

        public void toggle(boolean on, boolean isJar) {
            this.isJar = isJar;
            if (on) this.state = State.FIRST_ON;
            else this.state = State.FIRST_OFF;
        }

        public void reset() {
            this.state = State.OFF;
        }

        public boolean isOnAndTick(PlayerRenderer renderer) {
            switch (this.state) {
                case ON -> {
                    if (isJar) {
                        renderer.getModel().head.visible = false;
                        renderer.getModel().hat.visible = false;
                        return false;
                    } else return true;
                }
                case FIRST_ON -> {
                    this.oldShadowSize = renderer.shadowRadius;
                    this.state = State.ON;
                    if (isJar) {
                        renderer.getModel().head.visible = false;
                        renderer.getModel().hat.visible = false;
                        return false; // dont cancel render
                    } else {
                        renderer.shadowRadius = 0;
                        return true;
                    }
                }
                case FIRST_OFF -> {
                    renderer.shadowRadius = this.oldShadowSize;
                    renderer.getModel().head.visible = true;
                    renderer.getModel().hat.visible = true;
                    this.state = State.OFF;
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }

        public boolean isOn() {
            return this.state == State.ON || this.state == State.FIRST_ON;
        }

        private enum State {
            ON, OFF, FIRST_ON, FIRST_OFF
        }
    }

}
