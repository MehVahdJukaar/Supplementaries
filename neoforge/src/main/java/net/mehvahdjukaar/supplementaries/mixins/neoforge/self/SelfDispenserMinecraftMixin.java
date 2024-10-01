package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(DispenserMinecartEntity.class)
public abstract class SelfDispenserMinecraftMixin extends Minecart implements Container {

    @Unique
    private LazyOptional<?> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    protected SelfDispenserMinecraftMixin(EntityType<?> arg, Level arg2) {
        super(arg, arg2);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        return this.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER ? this.itemHandler.cast() : super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(this));
    }

}
