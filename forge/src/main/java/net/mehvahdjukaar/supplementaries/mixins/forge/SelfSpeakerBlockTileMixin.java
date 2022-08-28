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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpeakerBlockTile.class)
public abstract class SelfSpeakerBlockTileMixin extends BlockEntity {

    public SelfSpeakerBlockTileMixin(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if (CompatHandler.computercraft && CCCompatImpl.isPeripheralCap(cap)) {
            if(peripheral == null){
                peripheral = CCCompatImpl.getPeripheralSupplier((SpeakerBlockTile) (Object) this);
            }
            return peripheral.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(peripheral != null) {
            peripheral.invalidate();
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    @Unique
    private LazyOptional<Object> peripheral = null;
}
