package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.common.utils.SoapWashableHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

class SoapBehavior implements ItemUseOnBlockBehavior {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Functional.SOAP_ENABLED.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == ModRegistry.SOAP.get();
    }

    @Override
    public InteractionResult tryPerformingAction(Level level, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        if (SoapWashableHelper.tryWash(level, pos, level.getBlockState(pos))) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (!player.getAbilities().instabuild) stack.shrink(1);

                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                level.playSound(null, pos, ModSounds.SOAP_WASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}

