package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin extends Block {

    public CauldronBlockMixin(Properties properties) {
        super(properties);
    }


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

}