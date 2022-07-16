package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.state.BlockState;

public class FramedBlocksCompat {
    @ExpectPlatform
    public static BlockState getFramedFence() {
        throw new AssertionError();
    }
}
