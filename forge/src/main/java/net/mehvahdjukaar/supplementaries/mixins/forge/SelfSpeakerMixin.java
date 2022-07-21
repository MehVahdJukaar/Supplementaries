package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.CCCompatImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpeakerBlockTile.class)
public abstract class SelfSpeakerMixin extends BlockEntity {

    public SelfSpeakerMixin(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (CompatHandler.computercraft && CCCompatImpl.isPeripheralCap(cap)) {
            return peripheral.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        peripheral.invalidate();
    }

    @Unique
    private final LazyOptional<Object> peripheral = CompatHandler.computercraft ?
            CCCompatImpl.getPeripheralSupplier((SpeakerBlockTile) (Object) this) : LazyOptional.empty();

}
