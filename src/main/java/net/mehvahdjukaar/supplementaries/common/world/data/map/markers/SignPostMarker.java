package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.markers.NamedMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class SignPostMarker extends NamedMapBlockMarker<CustomMapDecoration> {


    public SignPostMarker() {
        super(CMDreg.SIGN_POST_DECORATION_TYPE);
    }

    public SignPostMarker(BlockPos pos, Component name) {
        super(CMDreg.SIGN_POST_DECORATION_TYPE, pos);
        this.name = name;
    }

    @Nullable
    public static SignPostMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            Component t = new TextComponent("");
            if (tile.up) t = tile.textHolder.getLine(0);
            if (tile.down && t.getString().isEmpty())
                t = tile.textHolder.getLine(1);
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
