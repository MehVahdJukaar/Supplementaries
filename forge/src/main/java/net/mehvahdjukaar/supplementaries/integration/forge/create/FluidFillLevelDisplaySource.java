package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.source.PercentOrProgressBarDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidFillLevelDisplaySource extends PercentOrProgressBarDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.sourceConfig().getInt("Mode") == 2) {
            if (context.getSourceTE() instanceof ISoftFluidTankProvider tp) {
                return Components.literal(tp.getSoftFluidTank().getCount() + " mBtl");
            }
        }
        return super.provideLine(context, stats);
    }

    @Override
    protected Float getProgress(DisplayLinkContext context) {
        BlockEntity te = context.getSourceTE();
        if (te instanceof ISoftFluidTankProvider tp) {
            return tp.getSoftFluidTank().getHeight(1);
        }
        return null;
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return context.sourceConfig().getInt("Mode") == 1;
    }

    @Override
    protected String getTranslationKey() {
        return "fluid_amount";
    }

    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (!isFirstLine) {
            builder.addSelectionScrollInput(
                    0,
                    120,
                    (si, l) -> si.forOptions(Lang.translatedOptions("display_source.fill_level", "percent", "progress_bar", "fluid_amount"))
                            .titled(Lang.translateDirect("display_source.fill_level.display")),
                    "Mode"
            );
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
