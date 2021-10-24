package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CauldronBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin extends Block {

    public CauldronBlockMixin(Properties properties) {
        super(properties);
    }

//TODO: do it the new way
    /*
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult rayTraceResult, CallbackInfoReturnable<InteractionResult> info) {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (!itemstack.isEmpty()) {
            int i = state.getValue(CauldronBlock.LEVEL);
            Item item = itemstack.getItem();
            if (i < 1) return;
            //clear flags
            if (item instanceof FlagItem) {
                if (BannerBlockEntity.getPatternCount(itemstack) > 0 && !world.isClientSide) {
                    ItemStack itemstack2 = itemstack.copy();
                    itemstack2.setCount(1);
                    BannerBlockEntity.removeLastPattern(itemstack2);
                    playerEntity.awardStat(Stats.CLEAN_BANNER);
                    if (!playerEntity.abilities.instabuild) {
                        itemstack.shrink(1);
                        ((CauldronBlock) state.getBlock()).setWaterLevel(world, pos, state, i - 1);
                    }

                    if (itemstack.isEmpty()) {
                        playerEntity.setItemInHand(hand, itemstack2);
                    } else if (!playerEntity.inventory.add(itemstack2)) {
                        playerEntity.drop(itemstack2, false);
                    } else if (playerEntity instanceof ServerPlayer) {
                        ((ServerPlayer) playerEntity).refreshContainer(playerEntity.inventoryMenu);
                    }
                }
                info.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));
            }
            if(item instanceof MapItem){
                if(!world.isClientSide){
                    MapItemSavedData data = MapItem.getOrCreateSavedData(itemstack,world);
                    if(data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).resetCustomDecoration();
                    }
                }
                if (!playerEntity.abilities.instabuild) {
                    ((CauldronBlock) state.getBlock()).setWaterLevel(world, pos, state, i - 1);
                }
                info.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));

            }
        }
    }
    */

}