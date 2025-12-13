package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.NoteBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

public class FlowerPotUtils {
    //server side is tag driven
    public static void setup() {
        registerFlowerPots();
    }

    @Contract
    @ExpectPlatform
    public static Block getEmptyPot(FlowerPotBlock fullPot) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isEmptyPot(Block b) {
        throw new AssertionError();
    }

    @ApiStatus.Internal
    //move to forge
    @ExpectPlatform
    public static void registerFlowerPots() {
        throw new AssertionError();
    }

}
