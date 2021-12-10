package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class FlagItem extends BurnableBlockItem {
    public FlagItem(Block block, Properties properties) {
        super(block, properties, 300);
    }

    public DyeColor getColor() {
        return ((FlagBlock) this.getBlock()).getColor();
    }

    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
    }

    //TODO: readd
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
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        ClientRegistry.registerISTER(consumer, FlagItemRenderer::new);
    }

}
