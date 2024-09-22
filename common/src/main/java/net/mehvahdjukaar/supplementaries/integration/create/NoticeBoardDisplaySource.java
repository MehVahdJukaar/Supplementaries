package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class NoticeBoardDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.getSourceBlockEntity() instanceof NoticeBoardBlockTile tile) {
            tile.updateText();
            return Component.literal(tile.getText().get(false));
        } else {
            return Component.empty();
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
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
        return "notice_board";
    }
}
