package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

interface BlockUseBehavior {

    /**
     * Used for permission checks on flan compat
     */
    default boolean altersWorld(){
        return false;
    }

    boolean isEnabled();

    boolean appliesToBlock(Block block);

    InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player, InteractionHand hand,
                                          ItemStack stack, BlockHitResult hit);
}

