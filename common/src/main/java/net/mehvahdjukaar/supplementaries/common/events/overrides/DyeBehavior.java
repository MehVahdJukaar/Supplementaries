package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

class DyeBehavior implements ItemUseOnBlockOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.DYE_BLOCKS.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return ForgeHelper.getColor(item.getDefaultInstance()) != null;
    }

    @Override
    public InteractionResult tryPerformingAction(Level level, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (Utils.mayBuild(player, hit.getBlockPos())) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Block newBlock = BlocksColorAPI.changeColor(state.getBlock(), ForgeHelper.getColor(stack));
            if (newBlock != null && !state.is(newBlock)) {
                BlockState newState = newBlock.withPropertiesOf(state);
                level.setBlockAndUpdate(pos, newState);
                if (player instanceof ServerPlayer serverPlayer) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                    level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}

