package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.item.*;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class FlagItem extends BlockItem {
    public FlagItem(Block block, Properties properties) {
        super(block, properties);
    }

    public DyeColor getColor() {
        return ((FlagBlock)this.getBlock()).getColor();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        //cauldron code
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if(state.getBlock() instanceof CauldronBlock){
            int i = state.getValue(CauldronBlock.LEVEL);
            if(i>0){
                Level world = context.getLevel();
                ItemStack stack = context.getItemInHand();
                if (BannerBlockEntity.getPatternCount(stack) > 0 && !world.isClientSide) {
                    Player player = context.getPlayer();
                    ItemStack itemstack2 = stack.copy();
                    itemstack2.setCount(1);
                    BannerBlockEntity.removeLastPattern(itemstack2);
                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                        ((CauldronBlock) state.getBlock()).setWaterLevel(world, context.getClickedPos(), state, i - 1);
                    }
                    if (stack.isEmpty()) {
                        player.setItemInHand(context.getHand(), itemstack2);
                    } else if (!player.inventory.add(itemstack2)) {
                        player.drop(itemstack2, false);
                    } else if (player instanceof ServerPlayer) {
                        ((ServerPlayer)player).refreshContainer(player.inventoryMenu);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 300;
    }
}
