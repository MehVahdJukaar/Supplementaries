package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class SpecialPlayers {

    //for pickle
    public static final List<UUID> DEVS = new ArrayList<>();
    //statues valid players
    public static final Map<String, UUID> STATUES = new HashMap<>();
    //custom globes
    public static final Map<String, ResourceLocation> GLOBES = new HashMap<>();

    //TODO: fix pickle not working on me offline
    static {
        addSpecialPlayer("Dev", true, false, true, "380df991-f603-344c-a090-369bad2a924a");
        addSpecialPlayer("MehVahdJukaar", true, false, true, "898b3a39-e486-405c-a873-d6b472dc3ba2", "TheEvilGolem");
        addSpecialPlayer("Capobianco", true, true, true, "90ceb598-9983-4da3-9cae-436d5afb9d81", "Plantkillable");
        addSpecialPlayer("ThugPug43", false, true, true, "98105ad8-080a-4d70-a5da-0cc27a833309");
        addSpecialPlayer("SylveticHearts", false, true, false, "bd337926-7396-4d3e-bfb9-7e562b077219");
        addSpecialPlayer("Toffanelly", false, true, false, null);
        addSpecialPlayer("Agrona", true, true, false, null, "Pancake", "Pancakes");
        addSpecialPlayer("StonkManHanz", false, false, true, "8b69ac73-b7d8-439f-972d-1ed43e583b47");
        addSpecialPlayer("Wais", false, true, false, null, "snowglobe");
        addSpecialPlayer("MylesTheChild", false, false, true, "ea92f2be-4bd1-4082-a9b3-e6a8fbd43063", "Wais");
        addSpecialPlayer("E_Y_E_", false, true, false, null, "Dark");
        addSpecialPlayer("Azrod_dovahkiin", false, false, true, "171ccd8a-3afe-4788-806d-ee643fe33a9c", "dragonborn");
    }

    private static void addSpecialPlayer(String name, boolean isDev, boolean hasGlobe, boolean hasStatue, String id, String... alias) {
        name = name.toLowerCase(Locale.ROOT);
        UUID onlineId;
        if (id == null) {
            onlineId = null;
        } else {
            onlineId = UUID.fromString(id);
        }
        if (isDev) {
            if (onlineId != null) DEVS.add(onlineId);
            DEVS.add(PlayerEntity.createPlayerUUID(name));

        }
        if (hasGlobe) {
            ResourceLocation texture = new ResourceLocation(Supplementaries.MOD_ID, "textures/entity/globes/globe_" + name + ".png");
            GLOBES.put(name, texture);
            for (String n : alias) {
                GLOBES.put(n.toLowerCase(Locale.ROOT), texture);
            }
        }
        if (hasStatue) {
            STATUES.put(name, onlineId);
            for (String n : alias) {
                STATUES.put(n.toLowerCase(Locale.ROOT), onlineId);
            }
        }
    }

}
