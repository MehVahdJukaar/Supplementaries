package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;

class EnderPearlBehavior extends AdditionalDispenserBehavior {

    protected EnderPearlBehavior() {
        super(Items.ENDER_PEARL);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        Level level = source.getLevel();
        ThrownEnderpearl pearl = PearlMarker.getPearlToDispenseAndPlaceMarker(source);
        Position position = DispenserBlock.getDispensePosition(source);
        pearl.setPos(position.x(), position.y(), position.z());

        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);

        pearl.shoot(direction.getStepX(), direction.getStepY() + 0.1F, direction.getStepZ(), this.getPower(), this.getUncertainty());
        level.addFreshEntity(pearl);

        stack.shrink(1);

        return InteractionResultHolder.success(stack);
    }


    @Override
    protected void playSound(BlockSource source, boolean success) {
        source.getLevel().levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, source.getPos(), 0);
    }

    protected float getUncertainty() {
        return 6.0F;
    }

    protected float getPower() {
        return 1.1F;
    }
}
