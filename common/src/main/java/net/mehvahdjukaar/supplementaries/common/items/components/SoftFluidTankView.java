package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

// immutable view of a SoftFluidTank
public class SoftFluidTankView {

    public static final Codec<SoftFluidTankView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SoftFluidStack.CODEC.fieldOf("fluid").forGetter(t -> t.inner.getFluid()),
            Codec.INT.fieldOf("capacity").forGetter(SoftFluidTankView::getCapacity)
    ).apply(instance, SoftFluidTankView::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoftFluidTankView> STREAM_CODEC = StreamCodec.composite(
            SoftFluidStack.STREAM_CODEC, t -> t.inner.getFluid(),
            ByteBufCodecs.INT, SoftFluidTankView::getCapacity,
            SoftFluidTankView::new
    );

    private final SoftFluidTank inner;

    private SoftFluidTankView(SoftFluidStack fluid, int capacity) {
        this.inner = SoftFluidTank.create(fluid, capacity);
    }

    private SoftFluidTankView(SoftFluidTank tank) {
        this.inner = tank.makeCopy();
    }

    public static SoftFluidTankView of(SoftFluidTank tank) {
        return new SoftFluidTankView(tank.makeCopy());
    }

    public SoftFluid getFluid() {
        return this.inner.getFluidValue();
    }

    public int getCapacity() {
        return this.inner.getCapacity();
    }

    public boolean isEmpty() {
        return this.inner.isEmpty();
    }

    public boolean isFull() {
        return this.inner.isFull();
    }

    public int getCount() {
        return this.inner.getFluidCount();
    }

    public int getFlowingColor(Level level) {
        return this.inner.getCachedFlowingColor(level, null);
    }

    public int getStillColor(Level level) {
        return this.inner.getCachedStillColor(level, null);
    }

    public void apply(SoftFluidTank fluidHolder) {
        fluidHolder.copyContent(this.inner);
    }
}
