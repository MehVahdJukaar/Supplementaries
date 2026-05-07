package net.mehvahdjukaar.supplementaries.reg.platform;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

//same as FluidBucketWrapper but with configurable count
public class FluidHandlerItemCap implements IFluidHandlerItem {
    protected final int tankVolume;
    protected final Item empty;
    protected final Item full;
    protected final Fluid fillContent;

    protected ItemStack container;

    public FluidHandlerItemCap(ItemStack container, int volume, Item empty, Item full, Fluid fillContent) {
        this.container = container;
        this.tankVolume = volume;
        this.empty = empty;
        this.full = full;
        this.fillContent = fillContent;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }


    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.is(fillContent);
    }

    private FluidStack getFluidInternal() {
        Item item = container.getItem();
        if (item == full) {
            return new FluidStack(fillContent, tankVolume);
        } else {
            return FluidStack.EMPTY;
        }
    }

    private void setFluidInternal(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            container = new ItemStack(empty);
        } else {
            container = new ItemStack(full);
        }
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluidInternal();
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
        if (container.getCount() != 1 || resource.getAmount() < tankVolume || !getFluidInternal().isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }

        if (action.execute()) {
            setFluidInternal(resource);
        }

        return tankVolume;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < tankVolume) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluidInternal();
        if (!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
            if (action.execute()) {
                setFluidInternal(FluidStack.EMPTY);
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

        FluidStack fluidStack = getFluidInternal();
        if (!fluidStack.isEmpty()) {
            if (action.execute()) {
                setFluidInternal(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }
}
