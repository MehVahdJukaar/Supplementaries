package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.NamedMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;

public class SignPostMarker extends NamedMapBlockMarker<CustomMapDecoration> {


    public SignPostMarker() {
        super(ModMapMarkers.SIGN_POST_DECORATION_TYPE);
    }

    public SignPostMarker(BlockPos pos, Component name) {
        super(ModMapMarkers.SIGN_POST_DECORATION_TYPE, pos);
        this.name = name;
    }

    @Nullable
    public static SignPostMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            Component t = Component.literal("");
            if (tile.getSignUp().active()) t = tile.getTextHolder().getLine(0);
            if (tile.getSignDown().active() && t.getString().isEmpty())
                t = tile.getTextHolder().getLine(1);
            if (t.getString().isEmpty()) t = null;
            return new SignPostMarker(pos, t);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomMapDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomMapDecoration(this.getType(), mapX, mapY, rot, name);
    }

}
