package net.mehvahdjukaar.supplementaries.reg.neoforge;

import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

//same as FluidBucketWrapper but with configurable count
public class FluidHandlerItemCap implements IFluidHandlerItem {
    protected final int tankVolume;
    protected final Item empty;

    protected ItemStack container;

    public FluidHandlerItemCap(ItemStack container, int volume, Item empty) {
        this.container = container;
        this.tankVolume = volume;
     this.empty = empty;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }


    public boolean canFillFluidType(FluidStack fluid) {
        if (fluid.is(Fluids.WATER) || fluid.is(Fluids.LAVA)) {
            return true;
        }
        return !fluid.getFluidType().getBucket(fluid).isEmpty();
    }

    public FluidStack getFluid() {
        Item item = container.getItem();
        if (item instanceof BucketItem) {
            return new FluidStack(((BucketItem) item).content, tankVolume);
        } else if (item instanceof MilkBucketItem && NeoForgeMod.MILK.isBound()) {
            return new FluidStack(NeoForgeMod.MILK.get(), tankVolume);
        } else {
            return FluidStack.EMPTY;
        }
    }

    protected void setFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty())
            container = new ItemStack(empty);
        else
            container = FluidUtil.getFilledBucket(fluidStack);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tankVolume;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < tankVolume || container.getItem() instanceof MilkBucketItem || !getFluid().isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }

        if (action.execute()) {
            setFluid(resource);
        }

        return tankVolume;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < tankVolume) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < tankVolume) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty()) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }
}
