package net.mehvahdjukaar.supplementaries.common.items;

import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public abstract class InstrumentItem extends Item {

    public InstrumentItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) {
            SongsManager.playRandomSong(stack, this, entity, getUseDuration(stack) - remainingUseDuration);
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity entity, int pTimeCharged) {
        SongsManager.clearCurrentlyPlaying(entity.getUUID());
    }

    public float getPitch(int note) {
        //noteblocks logic
        return (float) Math.pow(2.0D, (double) (note - 1 - 12) / 12.0D);
    }

    public float getVolume() {
        return 1;
    }


    public SoundEvent getSound() {
        return SoundEvents.NOTE_BLOCK_FLUTE;
    }

    //client stuff
    public void spawnNoteParticle(Level level, LivingEntity entity, int note) {
    }

    public final void playSongNotesOnClient(IntList notes, Player player) {
        for (int note : notes) {
            if (note > 0) {
                //always plays a sound for local player. this is becasue this method is calld on client side for other clients aswell
                //and playsound only plays if the given player is the local one
                player.level.playSound(Minecraft.getInstance().player, player.getX(), player.getY(), player.getZ(),
                        this.getSound(), SoundSource.PLAYERS, this.getVolume(), this.getPitch(note));

                this.spawnNoteParticle(player.level, player, note);
            }
        }
    }

}
