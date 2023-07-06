package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class TextHolderDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        BlockEntity te = context.getTargetBlockEntity();
        if (te instanceof ITextHolderProvider th) {
            var textHolder = th.getTextHolder();
            boolean changed = false;

            for (int i = 0; i < text.size() && i + line < textHolder.size(); ++i) {
                if (i == 0) {
                    reserve(i + line, te, context);
                }
                if (i > 0 && this.isReserved(i + line, te, context)) {
                    break;
                }
                textHolder.setMessage(i + line, text.get(i));
                changed = true;
            }
            if (changed) {
                context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
            }
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        var textHolder = ((ITextHolderProvider) context.getTargetBlockEntity()).getTextHolder();
        return new DisplayTargetStats(textHolder.size(), textHolder.getMaxLineCharacters(), this);
    }
}
