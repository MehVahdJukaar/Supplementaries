package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class RopeSlideSoundInstance extends AbstractTickableSoundInstance {

    private final Player player;
    private int ropeTicks;

    public RopeSlideSoundInstance(Player player) {
        super(ModSounds.ROPE_SLIDE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
        this.looping = true;
        this.delay = 1; //wait a second before starting
        this.volume = 0.0F;
        this.ropeTicks = 0;
    }

    @Override
    public void tick() {
        if (!this.player.isRemoved()) {
            if (player.onClimbable()) {
                BlockState b = player.getBlockStateOn();
                if (b.is(ModTags.FAST_FALL_ROPES)) {

                    this.x = this.player.getX();
                    this.y = this.player.getY();
                    this.z = this.player.getZ();

                    float downwardSpeed = -(float) player.getDeltaMovement().y;
                    float minPitch = 0.7f;
                    float maxPitch = 2;
                    float speedScaling = 0.5f;
                    float newPitch = Mth.clamp(0.5f + downwardSpeed * speedScaling, 0, maxPitch);
                    if (newPitch >= minPitch) {
                        this.ropeTicks++;

                        float minVolume = 0;
                        float maxVolume = 1;
                        float volumeScaling = 0.07f;
                        this.pitch = newPitch;
                        this.volume = Mth.clamp(ropeTicks * volumeScaling, minVolume, maxVolume);
                        return;
                    }
                }
            }
            this.pitch = 0.0F;
            this.volume = 0.0F;
            this.ropeTicks = 0;
        } else {
            this.stop();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.player.isSilent();
    }

}
