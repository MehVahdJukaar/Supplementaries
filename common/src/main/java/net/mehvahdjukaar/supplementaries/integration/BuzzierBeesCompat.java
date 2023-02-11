package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.ModConstants.CANDLE_HOLDER_NAME;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.getTab;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regWithItem;

public class BuzzierBeesCompat {

    public static void init() {

    }

    public static final Supplier<Block> SOUL_CANDLE_HOLDER = regWithItem(CANDLE_HOLDER_NAME + "_soul", () -> new CandleHolderBlock(null,
                    BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()), CompatObjects.SMALL_SOUL_FLAME),
            getTab(CreativeModeTab.TAB_DECORATIONS, CANDLE_HOLDER_NAME));

    public static void setupClient() {
        ClientPlatformHelper.registerRenderType(SOUL_CANDLE_HOLDER.get(), RenderType.cutout());

    }
}
