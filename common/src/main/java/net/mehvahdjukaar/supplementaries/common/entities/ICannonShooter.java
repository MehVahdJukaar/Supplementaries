package net.mehvahdjukaar.supplementaries.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;


public interface ICannonShooter {

    SoundEvent getCannonShootSound();

    default void onShotCannon(BlockPos pos) {
        if (this instanceof Entity e) {
            e.playSound(getCannonShootSound(), 1.0F, 1.2F);
        }
    }
}
