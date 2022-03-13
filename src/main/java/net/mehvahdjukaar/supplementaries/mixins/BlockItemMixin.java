package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.common.items.IPlaceableItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
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

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(BlockPlaceContext pContext, CallbackInfoReturnable<BlockState> cir) {
        Block override = this.getPlaceableBlock();
        if (override != null) {
            BlockState overrideBlockState = this.getPlacer().mimicGetPlacementState(pContext, override);
            if (overrideBlockState != null ) {
                cir.setReturnValue(overrideBlockState);
            }
        }
    }

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void place(BlockPlaceContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        Block override = this.getPlaceableBlock();
        if (override != null) {
            var result = this.getPlacer().mimicPlace(pContext, override,null);
            if (result.consumesAction()) {
                cir.setReturnValue(result);
            }
        }
    }

}
