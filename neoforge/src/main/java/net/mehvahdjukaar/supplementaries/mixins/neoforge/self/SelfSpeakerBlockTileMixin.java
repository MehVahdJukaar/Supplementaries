package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.neoforge.CCCompatImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpeakerBlockTile.class)
public abstract class SelfSpeakerBlockTileMixin extends BlockEntity {

    private SelfSpeakerBlockTileMixin(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction direction) {
        if (CompatHandler.COMPUTERCRAFT && CCCompatImpl.isPeripheralCap(cap)) {
            if (peripheral == null) {
                peripheral = CCCompatImpl.getPeripheralSupplier((SpeakerBlockTile) (Object) this);
            }
            return peripheral.cast();
        }
        return super.getCapability(cap, direction);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (peripheral != null) {
            peripheral.invalidate();
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    @Unique
    private LazyOptional<Object> peripheral = null;
}
