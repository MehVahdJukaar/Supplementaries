package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin extends Block {

    public CauldronBlockMixin(Properties properties) {
        super(properties);
    }


    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult rayTraceResult, CallbackInfoReturnable<ActionResultType> info) {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (!itemstack.isEmpty()) {
            int i = state.getValue(CauldronBlock.LEVEL);
            Item item = itemstack.getItem();
            if (i > 0 && item instanceof FlagItem) {
                if (BannerTileEntity.getPatternCount(itemstack) > 0 && !world.isClientSide) {
                    ItemStack itemstack2 = itemstack.copy();
                    itemstack2.setCount(1);
                    BannerTileEntity.removeLastPattern(itemstack2);
                    playerEntity.awardStat(Stats.CLEAN_BANNER);
                    if (!playerEntity.abilities.instabuild) {
                        itemstack.shrink(1);
                        ((CauldronBlock) state.getBlock()).setWaterLevel(world, pos, state, i - 1);
                    }

                    if (itemstack.isEmpty()) {
                        playerEntity.setItemInHand(hand, itemstack2);
                    } else if (!playerEntity.inventory.add(itemstack2)) {
                        playerEntity.drop(itemstack2, false);
                    } else if (playerEntity instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) playerEntity).refreshContainer(playerEntity.inventoryMenu);
                    }
                }
                info.setReturnValue(ActionResultType.sidedSuccess(world.isClientSide));
            }
        }
    }

}