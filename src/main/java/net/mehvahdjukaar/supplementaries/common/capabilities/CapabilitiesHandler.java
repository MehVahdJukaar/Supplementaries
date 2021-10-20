package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.jetbrains.annotations.Nullable;

public class CapabilitiesHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(ICatchableMob.class, new DummyStorage(), DummyCatchableMobCap::new);

        CapabilityManager.INSTANCE.register(IAntiqueTextProvider.class, new AntiqueTextStorage(), DummyAntiqueInkCap::new);
    }

    //don't need to store anything
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

    public static class DummyAntiqueInkCap implements IAntiqueTextProvider{

        @Override
        public boolean hasAntiqueInk() {
            return false;
        }

        @Override
        public void setAntiqueInk(boolean hasInk) {
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