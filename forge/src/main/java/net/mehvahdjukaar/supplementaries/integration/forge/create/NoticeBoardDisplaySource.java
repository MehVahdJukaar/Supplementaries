package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.source.SingleLineDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class NoticeBoardDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.getSourceTE() instanceof NoticeBoardBlockTile tile) {
            tile.updateText();
            return Component.literal(tile.getText());
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
