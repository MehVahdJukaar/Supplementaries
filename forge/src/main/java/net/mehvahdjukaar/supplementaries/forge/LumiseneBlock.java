package net.mehvahdjukaar.supplementaries.forge;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class LumiseneBlock extends LiquidBlock {

    public LumiseneBlock(Supplier<? extends FlowingFluid> supplier, Properties arg) {
        super(supplier, arg);
    }

}
