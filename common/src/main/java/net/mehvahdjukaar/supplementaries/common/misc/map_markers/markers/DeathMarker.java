package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Set;

public class DeathMarker {

    public static Set<CustomMapDecoration> getForPlayer(MapDecorationType<?, ?> type, Player player, MapItemSavedData data) {
        var v = player.getLastDeathLocation();
        if (v.isPresent() && data.dimension.equals(v.get().dimension())) {
            if (CommonConfigs.Tweaks.DEATH_MARKER.get().isOn(player)) {
                CustomMapDecoration decorationFromMarker = type.getDefaultMarker(v.get().pos())
                        .createDecorationFromMarker(data.scale, data.centerX, data.centerZ, data.locked().dimension, data.locked);
                if (decorationFromMarker != null) {
                    decorationFromMarker.setDisplayName(Component.translatable("message.supplementaries.death_marker"));
                    return Set.of(decorationFromMarker);
                }
            }
        }
        return Set.of();

    }
}
