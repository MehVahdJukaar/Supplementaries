package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;

import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
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

    public static final Map<UUID, PickleValues> PICKLE_PLAYERS = new HashMap<>();

    static {
        for (UUID id : Credits.INSTANCE.getDevs()) PICKLE_PLAYERS.put(id, new PickleValues());
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
                NetworkHandler.CHANNEL.sendToClientPlayer((ServerPlayer) player,
                        new PicklePacket.ClientBound(id, on));
            }
        }
    }

    public static boolean isDev(UUID id) {
        return PICKLE_PLAYERS.containsKey(id);
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
            ON, OFF, FIRST_ON, FIRST_OFF
        }
    }

}
