package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWaxable {

    boolean isWaxed();

    void setWaxed(boolean b);

    //callable on both sides
    default ItemInteractionResult tryWaxing(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (stack.getItem() instanceof HoneycombItem) {
            if (isWaxed()) {
                level.playSound(null, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
                return ItemInteractionResult.FAIL;
            }
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS);

            stack.consume(1, player);

            //server logic. stuff should be sent my packets here
            if (player instanceof ServerPlayer serverPlayer) {
                this.setWaxed(true);

                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                NetworkHelper.sendToAllClientPlayersInParticleRange((ServerLevel) level, pos,
                        new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Kind.WAX_ON));
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
