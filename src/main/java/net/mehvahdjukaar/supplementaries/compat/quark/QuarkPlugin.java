package net.mehvahdjukaar.supplementaries.compat.quark;


import net.mehvahdjukaar.supplementaries.items.ItemsUtil;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.addons.oddities.item.BackpackItem;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.building.block.WoodPostBlock;
import vazkii.quark.content.tools.item.AncientTomeItem;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;

public class QuarkPlugin {

    public static boolean hasQButtonOnRight() {
        return GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton;
    }


    //this should have been implemented in the post block updateShape method
    public static @Nullable BlockState updateWoodPostShape(BlockState post, Direction facing, BlockState facingState) {
        if (post.getBlock() instanceof WoodPostBlock) {
            Direction.Axis axis = post.getValue(WoodPostBlock.AXIS);
            if (facing.getAxis() != axis) {
                boolean chain = (facingState.getBlock() instanceof ChainBlock &&
                        facingState.getValue(BlockStateProperties.AXIS) == facing.getAxis());
                return post.setValue(WoodPostBlock.CHAINED[facing.ordinal()], chain);
            }
        }
        return null;
    }

    public static boolean isTome(Item item) {
        return item instanceof AncientTomeItem;
    }


    public static void registerTooltipComponent() {
        MinecraftForgeClient.registerTooltipComponentFactory(ItemsUtil.InventoryTooltip.class, InventoryTooltipComponent::new);
    }

    public static boolean isDoubleDoorEnabled() {
        return ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class);
    }

    public static int getSacksInBackpack(ItemStack stack) {
        int j = 0;
        if (stack.getItem() instanceof BackpackItem) {
            LazyOptional<IItemHandler> handlerOpt = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handlerOpt.isPresent()) {
                IItemHandler handler = handlerOpt.orElse(null);
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack slotItem = handler.getStackInSlot(i);
                    if (slotItem.getItem() instanceof SackItem) {
                        j++;
                    }
                }
            }
        }
        return j;
    }
}
