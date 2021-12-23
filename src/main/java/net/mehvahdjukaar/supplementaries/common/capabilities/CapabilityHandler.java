package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.configs.RegistryConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;

public class CapabilityHandler {

    public static final Capability<ICatchableMob> CATCHABLE_MOB_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IAntiqueTextProvider> ANTIQUE_TEXT_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ICatchableMob.class);
        event.register(IAntiqueTextProvider.class);
    }

    private static final ResourceLocation ANTIQUE_INK = new ResourceLocation(Supplementaries.MOD_ID, "antique_ink");

    public static final boolean ANTIQUE_CAP_ENABLED = RegistryConfigs.reg.ANTIQUE_INK_ENABLED.get();

    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (ANTIQUE_CAP_ENABLED && event.getObject() instanceof SignBlockEntity) {
            event.addCapability(ANTIQUE_INK, new AntiqueInkProvider());
        }
    }

    //actual capability provider (which provides itself as a cap instance)
    public static class AntiqueInkProvider implements IAntiqueTextProvider, ICapabilitySerializable<CompoundTag> {

        private boolean hasAntiqueInk = false;

        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
            return capability == ANTIQUE_TEXT_CAP ?
                    LazyOptional.of(() -> this).cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("ink", this.hasAntiqueInk);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            this.hasAntiqueInk = tag.getBoolean("ink");
        }

        @Override
        public boolean hasAntiqueInk() {
            return this.hasAntiqueInk;
        }

        @Override
        public void setAntiqueInk(boolean hasInk) {
            this.hasAntiqueInk = hasInk;
        }
    }


    public static void doStuff(BlockEntity tile, Runnable callable) {
        tile.getCapability(ANTIQUE_TEXT_CAP).ifPresent(c -> {
            if (c.hasAntiqueInk()) {
                IAntiqueTextProvider FONT = (IAntiqueTextProvider) (Minecraft.getInstance().font);
                FONT.setAntiqueInk(true);
                callable.run();
                ;
                //antiqueFontActive = true;
            }
        });
    }

}
