package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CCCompat {

    @ExpectPlatform
    public static boolean checkForPrintedBook(Item item) {
        throw new AssertionError();
    }

    public static void initialize() {
    }

    @ExpectPlatform
    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties p) {
        throw new AssertionError();
    }
}
