package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

class BellChainBehavior implements BlockUseOverride {

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.BELL_CHAIN.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return block instanceof ChainBlock;
    }

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player,
                                                 InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        //bell chains
        if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (RopeBlock.findAndRingBell(world, pos, player, 0, s -> s.getBlock() instanceof ChainBlock && s.getValue(ChainBlock.AXIS) == Direction.Axis.Y)) {
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
