package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FramedBlocksCompat {

    @PlatformImpl
    public static BlockState getFramedFence() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Block tryGettingFramedBlock(Block targetBlock, Level world, BlockPos blockpos) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean interactWithFramedSignPost(SignPostBlockTile tile, Player player, InteractionHand handIn, ItemStack itemstack, Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @ClientOnly
    @PlatformImpl
    public static ExtraModelData getModelData(BlockState mimic) {
        throw new AssertionError();
    }
}
