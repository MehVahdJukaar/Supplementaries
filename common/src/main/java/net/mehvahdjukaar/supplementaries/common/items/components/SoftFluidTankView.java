package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

// immutable view of a SoftFluidTank
public class SoftFluidTankView implements TooltipProvider {

    public static final Codec<SoftFluidTankView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SoftFluidStack.CODEC.fieldOf("fluid").forGetter(t -> t.inner.getFluid()),
            Codec.INT.fieldOf("capacity").forGetter(SoftFluidTankView::getCapacity)
    ).apply(instance, SoftFluidTankView::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoftFluid>> SF =
            new StreamCodec<RegistryFriendlyByteBuf, Holder<SoftFluid>>() {
                StreamCodec<RegistryFriendlyByteBuf, Holder<SoftFluid>> inner = ByteBufCodecs.holderRegistry(SoftFluidRegistry.KEY);

                @Override
                public Holder<SoftFluid> decode(RegistryFriendlyByteBuf object) {
                    return inner.decode(object);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf object, Holder<SoftFluid> object2) {
                    try{
                        inner.encode(object, object2);

                    }catch (Exception e){
                        e.printStackTrace();
                        inner.encode(object, object2);
                    }
                }
            };
    public static final StreamCodec<RegistryFriendlyByteBuf, SoftFluidStack> SFS = StreamCodec.composite(SF,
            SoftFluidStack::getHolder, ByteBufCodecs.VAR_INT, SoftFluidStack::getCount,
            DataComponentPatch.STREAM_CODEC, (s) -> s.getComponents().asPatch(), SoftFluidStack::of);


    public static final StreamCodec<RegistryFriendlyByteBuf, SoftFluidTankView> STREAM_CODEC = StreamCodec.composite(
            SFS, t -> t.inner.getFluid(),
            ByteBufCodecs.INT, SoftFluidTankView::getCapacity,
            SoftFluidTankView::new
    ); //server tank: 44351 client: 44365 44370 stack: 44366

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

    public SoftFluidTank toMutable() {
        return this.inner.makeCopy();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (!this.inner.isEmpty()) {
            SoftFluidStack fluid = this.inner.getFluid();
            Component fluidName = fluid.getDisplayName();

            tooltipAdder.accept(Component.translatable("message.supplementaries.fluid_tooltip",
                    fluidName, fluid.getCount()).withStyle(ChatFormatting.GRAY));

            PotionContents contents = fluid.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                contents.addPotionTooltip(tooltipAdder, 1.0F, context.tickRate());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoftFluidTankView tankView)) return false;
        return inner.getFluid().isSameFluidSameComponents(tankView.inner.getFluid());
    }

    @Override
    public int hashCode() {
        return inner.getFluid().hashCode();
    }
}
