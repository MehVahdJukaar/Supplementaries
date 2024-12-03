package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy {

    public static Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }
}
