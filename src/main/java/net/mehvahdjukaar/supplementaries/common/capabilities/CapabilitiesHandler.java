package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CapabilitiesHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(ICatchableMob.class, new DummyStorage(), DummyCatchableMobCap::new);

        CapabilityManager.INSTANCE.register(IAntiqueTextProvider.class, new AntiqueTextStorage(), SimpleAntiqueInkCap::new);
    }


    private static class AntiqueTextStorage implements Capability.IStorage<IAntiqueTextProvider> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IAntiqueTextProvider> capability, IAntiqueTextProvider instance, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            tag.putBoolean("AntiqueInk",instance.hasAntiqueInk());
            return tag;
        }

        @Override
        public void readNBT(Capability<IAntiqueTextProvider> capability, IAntiqueTextProvider instance, Direction side, INBT nbt) {
            instance.setAntiqueInk(nbt instanceof CompoundNBT && ((CompoundNBT) nbt).getBoolean("AntiqueInk"));
        }
    }

    public static class AntiqueInkProvider extends SimpleAntiqueInkCap implements ICapabilityProvider {
        public AntiqueInkProvider() {
            int a =1;
        }

        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
            return capability == SupplementariesCapabilities.ANTIQUE_TEXT_CAP ?
                    LazyOptional.of(() -> this).cast() : LazyOptional.empty();
        }
    }

    private static class SimpleAntiqueInkCap implements IAntiqueTextProvider{

        private boolean ink;

        @Override
        public boolean hasAntiqueInk() {
            return ink;
        }

        @Override
        public void setAntiqueInk(boolean hasInk) {
            this.ink = hasInk;
        }
    }

    //don't need to store anything
    private static class DummyStorage implements Capability.IStorage<ICatchableMob> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<ICatchableMob> capability, ICatchableMob instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<ICatchableMob> capability, ICatchableMob instance, Direction side, INBT nbt) {
        }
    }

    public static class DummyCatchableMobCap implements ICatchableMob{

        @Override
        public boolean canBeCaughtWithJar() {
            return false;
        }

        @Override
        public boolean canBeCaughtWithTintedJar() {
            return false;
        }

        @Override
        public boolean canBeCaughtWithCage() {
            return false;
        }

        @Override
        public Entity getEntity() {
            return null;
        }
    }

}