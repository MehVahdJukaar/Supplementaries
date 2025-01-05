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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

//TODO: use more
public interface IGlowable {


    boolean isGlowing();

    void setGlowing(boolean b);

    //TODO: move to static utility classmaybe?
    //callable on both sides
    default ItemInteractionResult tryGlowing(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.GLOW_INK_SAC)) {
            if (isGlowing()) {
                level.playSound(null, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
                return ItemInteractionResult.FAIL;
            }
            level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS);

            stack.consume(1, player);

            //server logic. stuff should be sent my packets here
            if (player instanceof ServerPlayer serverPlayer) {
                this.setGlowing(true);

                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                NetworkHelper.sendToAllClientPlayersInParticleRange((ServerLevel) level, pos,
                        new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Kind.GLOW_ON));
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
