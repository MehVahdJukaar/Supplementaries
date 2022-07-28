package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FarmersDelightCompat {

    @ExpectPlatform
    public static InteractionResult onCakeInteract(BlockState state, BlockPos pos, Level level, ItemStack itemstack) {
        throw new AssertionError();
    }
}
