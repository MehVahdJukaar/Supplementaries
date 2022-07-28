package net.mehvahdjukaar.supplementaries.integration.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FarmersDelightCompatImpl {
    public static InteractionResult onCakeInteract(BlockState state, BlockPos pos, Level level, ItemStack itemstack) {
        return InteractionResult.PASS;
    }
}
