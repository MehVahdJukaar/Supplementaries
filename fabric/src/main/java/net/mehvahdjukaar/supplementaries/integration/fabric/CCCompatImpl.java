package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CCCompatImpl {
    public static boolean isPrintedBook(Item item) {
        throw new UnsupportedOperationException();
    }

    public static void initialize() {
        throw new UnsupportedOperationException();
    }

    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties p) {
        throw new UnsupportedOperationException();
    }

    public static int getPages(ItemStack itemstack) {
        throw new UnsupportedOperationException();
    }

    public static String[] getText(ItemStack itemstack) {
        throw new UnsupportedOperationException();
    }
}
