package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;

public class ChestMarker extends MapBlockMarker<CustomDecoration> {

    public ChestMarker() {
        super(CMDreg.CHEST_DECORATION_TYPE);
    }

    public ChestMarker(BlockPos pos) {
        super(CMDreg.CHEST_DECORATION_TYPE, pos);
        this.setPos(pos);
    }

    @Nullable
    public static ChestMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).is(Tags.Blocks.CHESTS)) {
            return new ChestMarker(pos);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(), mapX, mapY, rot, null);
    }
}
