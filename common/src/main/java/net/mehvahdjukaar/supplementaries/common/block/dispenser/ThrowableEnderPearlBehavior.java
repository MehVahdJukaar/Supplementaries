package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

class ThrowableEnderPearlBehavior extends ProjectileBehavior {

    protected ThrowableEnderPearlBehavior() {
        super(Items.ENDER_PEARL);
    }

    //TODO: maybe replace with level event dispenser shoot sound
    @Override
    protected SoundEvent getSound() {
        return SoundEvents.ENDER_PEARL_THROW;
    }

    @Override
    protected Projectile getProjectileEntity(BlockSource source, Position position, ItemStack stackIn) {
        return PearlMarker.createPearlToDispenseAndPlaceMarker(source, position);
    }
}
