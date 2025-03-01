package net.mehvahdjukaar.supplementaries.integration.neoforge.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GlobeDisplaySource extends SingleLineDisplaySource {
    public static final MutableComponent EMPTY = Component.literal("--,--");

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.getSourceBlockEntity() instanceof GlobeBlockTile tile) {
            BlockPos pos = context.getSourcePos();
            return Component.literal("X: " + pos.getX() + ", Z: " + pos.getZ());
        } else {
            return EMPTY;
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
        return "Instant";
    }

    @Override
    protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
        return new FlapDisplaySection(size * 7.0F, "instant", false, false);
    }

    @Override
    protected String getTranslationKey() {
        return "world_position";
    }
}
