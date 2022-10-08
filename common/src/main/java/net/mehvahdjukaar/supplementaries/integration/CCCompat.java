package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CCCompat {

    @ExpectPlatform
    public static void setup() {
    }

    @ExpectPlatform
    public static boolean isPrintedBook(Item item) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties p) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getPages(ItemStack itemstack) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static String[] getText(ItemStack itemstack) {
        throw new AssertionError();
    }
}
