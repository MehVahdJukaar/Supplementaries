package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CCCompatImpl {
    public static boolean isPrintedBook(Item item) {
        return false;
    }

    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties p) {
        return new SpeakerBlock(p);
    }

    public static int getPages(ItemStack itemstack) {
        return 0;
    }

    public static String[] getText(ItemStack itemstack) {
        return new String[]{};
    }

    public static void setup() {
    }
}
