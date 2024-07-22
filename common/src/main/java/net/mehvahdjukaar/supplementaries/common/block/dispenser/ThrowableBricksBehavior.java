package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

class ThrowableBricksBehavior extends ProjectileBehavior {

    protected ThrowableBricksBehavior(Item item) {
        super(item);
    }

    @Override
    protected Projectile getProjectileEntity(BlockSource source, Position position, ItemStack stackIn) {
        return new ThrowableBrickEntity(source.getLevel(), position.x(), position.y(), position.z());
    }

    @Override
    protected SoundEvent getSound() {
        return ModSounds.BRICK_THROW.get();
    }

    @Override
    protected float getProjectileInaccuracy() {
        return 7.0F;
    }

    //TODO: fix throwable bricks rendering glitchyness
    @Override
    protected float getProjectileVelocity() {
        return 0.8F;
    }

}

