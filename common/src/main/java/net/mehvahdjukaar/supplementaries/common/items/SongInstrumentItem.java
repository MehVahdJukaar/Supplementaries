package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundFluteParrotsPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class SongInstrumentItem extends Item {

    protected SongInstrumentItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!world.isClientSide) {
            NetworkHelper.sendToAllClientPlayersTrackingEntityAndSelf(player, new ClientBoundFluteParrotsPacket(player, true));
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) {
            SongsManager.playRandomSong(stack, this, entity, getUseDuration(stack, entity) - remainingUseDuration);
            if (remainingUseDuration % 10 == 0) {
                level.gameEvent(entity, GameEvent.INSTRUMENT_PLAY, entity.position());
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity entity, int pTimeCharged) {
        SongsManager.clearCurrentlyPlaying(entity.getUUID());
        if (!pLevel.isClientSide) {
            NetworkHelper.sendToAllClientPlayersTrackingEntity(entity, new ClientBoundFluteParrotsPacket(entity, false));
        }
    }

    public float getPitch(int note) {
        //noteblocks logic
        return (float) Math.pow(2.0D, (note - 1 - 12) / 12.0D);
    }

    public float getVolume() {
        return 1;
    }


    public SoundEvent getSound() {
        return SoundEvents.NOTE_BLOCK_FLUTE.value();
    }

    //client stuff
    public void spawnNoteParticle(Level level, LivingEntity entity, int note) {
    }

}
