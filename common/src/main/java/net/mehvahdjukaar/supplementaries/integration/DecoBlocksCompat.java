package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DecoBlocksCompat {

    @ExpectPlatform
    public static boolean isBrazier(Block b) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canLightBrazier(BlockState state) {
        throw new AssertionError();
    }
}
