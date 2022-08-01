package net.mehvahdjukaar.supplementaries.common.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.JarItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.IColored;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FlagItem extends WoodBasedBlockItem implements IColored<Block>, ICustomItemRendererProvider {

    public FlagItem(Block block, Properties properties) {
        super(block, properties, 300);
    }

    @Override
    public DyeColor getColor() {
        return ((FlagBlock) this.getBlock()).getColor();
    }

    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
    }

    //TODO: turn this into a new cauldron interaction or add back
    /*
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //cauldron code
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getBlock() instanceof CauldronBlock) {
            int i = state.getValue(CauldronBlock.LEVEL);
            if (i > 0) {
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
                        ((ServerPlayer) player).refreshContainer(player.inventoryMenu);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }
     */

    @Override
    public @Nullable Map<DyeColor, Supplier<Block>> getItemColorMap() {
        return ModRegistry.FLAGS;
    }

    @Override
    public Supplier<ItemStackRenderer> getRendererFactory() {
        return FlagItemRenderer::new;
    }
}
