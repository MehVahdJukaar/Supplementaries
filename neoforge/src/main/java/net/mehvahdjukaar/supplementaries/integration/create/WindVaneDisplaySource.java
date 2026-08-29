package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

public class WindVaneDisplaySource extends SingleLineDisplaySource {
    private static final MutableComponent UNKNOWN = Component.literal("--");
    private static final String[] CONDITIONS = {"clear", "rain", "thunder", "wind_charged"};

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof WindVaneBlockTile)) return UNKNOWN;
        BlockState state = context.level().getBlockState(context.getSourcePos());
        if (!state.hasProperty(WindVaneBlock.WIND_STRENGTH)) return UNKNOWN;
        int strength = state.getValue(WindVaneBlock.WIND_STRENGTH);
        return Component.translatable("supplementaries.display_source.weather." + CONDITIONS[strength]);
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
        return "weather";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
