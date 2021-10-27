package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IAntiqueTextProvider {

    boolean hasAntiqueInk();

    void setAntiqueInk(boolean hasInk);
}
