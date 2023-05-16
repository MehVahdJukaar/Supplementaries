package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.source.SingleLineDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.minecraft.network.chat.MutableComponent;

public class ItemDisplayDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        MutableComponent combined = EMPTY_LINE.copy();

        if (context.getSourceTE() instanceof ItemDisplayTile te && !te.isEmpty()) {
            combined = combined.append(te.getDisplayedItem().getHoverName());
        }
        //else if(context.level().getBlockState(context.getSourcePos()) instanceof WorldlyContainerHolder wc){
        //    combined = combined.append(wc.getContainer())
        //}
        return combined;
    }

    @Override
    public int getPassiveRefreshTicks() {
        return 20;
    }

    @Override
    protected String getTranslationKey() {
        return "item_name";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
        return "Number";
    }
}
