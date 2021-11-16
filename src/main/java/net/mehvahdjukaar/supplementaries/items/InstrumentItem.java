package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.world.songs.SongsManager;
import net.minecraft.client.multiplayer.ClientLevel;
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

import java.util.Random;

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
        SongsManager.playRandomSong(this, entity,getUseDuration(stack) - remainingUseDuration);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity entity, int pTimeCharged) {
        SongsManager.clearCurrentlyPlaying(entity.getUUID());
    }

    public float getPitch(int note) {
        //noteblocks logic
        return (float)Math.pow(2.0D, (double)(note-1 - 12) / 12.0D);
    }

    public float getVolume() {
        return 1;
    }

    public void spawnNoteParticle(ClientLevel level, LivingEntity entity, int note) {
    }

    public SoundEvent getSound() {
        return SoundEvents.NOTE_BLOCK_FLUTE;
    }

    public final void playNoteAtEntity(LivingEntity entity, int note){
        Level level = entity.level;
        if(level.isClientSide) {
            this.spawnNoteParticle((ClientLevel) level, entity, note);
        }
        playNoteAt(entity.getX(), entity.getEyeY(), entity.getZ(), level, note, entity.getSoundSource());
    }

    public final void playNoteAt(double x, double y, double z, Level level, int note, SoundSource source) {
        //Random rand = level.random;
        //double sX = x + 0*(rand.nextDouble() - 0.5D) * 0.15D;
        //double sY = y + 0*0.15D;
        //double sZ = z + 0*(rand.nextDouble() - 0.5D) * 0.15D;

        level.playSound(null, x, y, z, this.getSound(), source, this.getVolume(), this.getPitch(note));
    }
}
