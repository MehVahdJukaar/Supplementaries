package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.cctweaked.CCPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpeakerBlockTile.class)
public abstract class SelfSpeakerMixin extends BlockEntity {

    public SelfSpeakerMixin(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @NotNull
    public LazyOptional<Object> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
        return peripheral;
    }

    @Unique
    private final LazyOptional<Object> peripheral = CompatHandler.computercraft ?
            CCPlugin.getPeripheralSupplier((SpeakerBlockTile) (Object) this) : LazyOptional.empty();

}
