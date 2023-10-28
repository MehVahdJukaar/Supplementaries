package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class SignPostMarker extends SimpleMapBlockMarker {

    public SignPostMarker() {
        super(ModMapMarkers.SIGN_POST_DECORATION_TYPE);
    }

    public SignPostMarker(BlockPos pos, Component name) {
        super(ModMapMarkers.SIGN_POST_DECORATION_TYPE);
        this.setName(name);
        this.setPos(pos);
    }

    @Nullable
    public static SignPostMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            Component t = Component.literal("");
            if (tile.getSignUp().active()) t = tile.getTextHolder(0).getMessage(0, false);
            if (tile.getSignDown().active() && t.getString().isEmpty())
                t = tile.getTextHolder(1).getMessage(0, false);
            if (t.getString().isEmpty()) t = null;
            return new SignPostMarker(pos, t);
        } else {
            return null;
        }
    }

}
