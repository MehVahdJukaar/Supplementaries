package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.impl.items.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FlagItem extends WoodBasedBlockItem implements IColored {


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

    @Override
    public @Nullable Map<DyeColor, RegistryObject<Item>> getItemColorMap() {
        return ModRegistry.FLAGS_ITEMS;
    }
}
