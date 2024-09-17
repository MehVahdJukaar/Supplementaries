package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FramedBlocksCompatImpl {
    public static BlockState getFramedFence() {
        return null;
    }

    public static Block tryGettingFramedBlock(Block targetBlock, Level world, BlockPos blockpos) {
        return null;
    }

    public static boolean interactWithFramedSignPost(SignPostBlockTile tile, Player player, InteractionHand handIn, ItemStack itemstack, Level level, BlockPos pos) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static ExtraModelData getModelData(BlockState mimic) {
        return null;
    }
}
