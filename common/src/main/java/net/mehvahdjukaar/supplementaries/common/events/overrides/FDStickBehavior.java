package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

class FDStickBehavior implements BlockUseOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.PLACEABLE_STICKS.get() && CompatHandler.FARMERS_DELIGHT;
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return block == CompatObjects.TOMATOES.get();
    }

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player,
                                                 InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (stack.getItem() == Items.STICK) {
            var tomato = FarmersDelightCompat.getStickTomato();
            if (tomato != null) {
                return InteractEventsHandler.replaceSimilarBlock(tomato,
                        player, stack, pos, level, state, SoundType.WOOD, BlockStateProperties.AGE_3);
            }
        }
        return InteractionResult.PASS;
    }
}

