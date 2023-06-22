package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWaxable {

    boolean isWaxed();

    void setWaxed(boolean b);

    //callable on both sides
    default InteractionResult tryWaxing(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (isWaxed()) {
            level.playSound(null, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof HoneycombItem) {

            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            //server logic. stuff should be sent my packets here
            if (player instanceof ServerPlayer serverPlayer) {
                this.setWaxed(true);

                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                NetworkHandler.CHANNEL.sendToAllClientPlayersInRange(level, pos, 64,
                        new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.EventType.WAX_ON));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
