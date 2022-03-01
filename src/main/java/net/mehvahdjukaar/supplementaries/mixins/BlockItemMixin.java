package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.IPlaceableItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item implements IPlaceableItem {

    public BlockItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Shadow
    protected abstract boolean canPlace(BlockPlaceContext pContext, BlockState pState);

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(BlockPlaceContext pContext, CallbackInfoReturnable<BlockState> cir) {
        BlockItem override = this.getBlockItemOverride();
        if (override != null) {
            BlockState overrideBlockState = override.getBlock().getStateForPlacement(pContext);
            if (overrideBlockState != null && this.canPlace(pContext, overrideBlockState)) {
                cir.setReturnValue(overrideBlockState);
            }
        }
    }

}
