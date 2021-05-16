package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class FlagItem extends BlockItem {
    public FlagItem(Block block, Properties properties) {
        super(block, properties);
    }

    public DyeColor getColor() {
        return ((FlagBlock)this.getBlock()).getColor();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
    }


    @Override
    public ActionResultType useOn(ItemUseContext context) {
        //cauldron code
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if(state.getBlock() instanceof CauldronBlock){
            int i = state.getValue(CauldronBlock.LEVEL);
            if(i>0){
                World world = context.getLevel();
                ItemStack stack = context.getItemInHand();
                if (BannerTileEntity.getPatternCount(stack) > 0 && !world.isClientSide) {
                    PlayerEntity player = context.getPlayer();
                    ItemStack itemstack2 = stack.copy();
                    itemstack2.setCount(1);
                    BannerTileEntity.removeLastPattern(itemstack2);
                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                        ((CauldronBlock) state.getBlock()).setWaterLevel(world, context.getClickedPos(), state, i - 1);
                    }
                    if (stack.isEmpty()) {
                        player.setItemInHand(context.getHand(), itemstack2);
                    } else if (!player.inventory.add(itemstack2)) {
                        player.drop(itemstack2, false);
                    } else if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)player).refreshContainer(player.inventoryMenu);
                    }
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }
}
