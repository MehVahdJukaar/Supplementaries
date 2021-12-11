package net.mehvahdjukaar.supplementaries.compat.quark;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.items.ItemsUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.building.block.WoodPostBlock;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;
import vazkii.quark.content.tools.item.AncientTomeItem;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;

public class QuarkPlugin {
    private static final ResourceLocation SACK_CAP = new ResourceLocation(Supplementaries.MOD_ID, "sack_drop_in");
    private static final ResourceLocation SAFE_CAP = new ResourceLocation(Supplementaries.MOD_ID, "safe_drop_in");

    private static final ResourceLocation JAR_CAP = new ResourceLocation(Supplementaries.MOD_ID, "jar_drop_in");

    public static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        Item i = event.getObject().getItem();
//        if(i == ModRegistry.SACK_ITEM.get())
//            event.addCapability(SACK_CAP, new SackDropIn());
//        else if (i == ModRegistry.SAFE_ITEM.get())
//            event.addCapability(SAFE_CAP, new SafeDropIn());


        //else if (i instanceof JarItem || i instanceof EmptyJarItem)
        //    event.addCapability(JAR_CAP, new JarDropIn());
    }

    public static boolean hasQButtonOnRight() {
        return GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton;
    }


    //this should have been implemented in the post block updateShape method
    public static @Nullable BlockState updateWoodPostShape(BlockState post, Direction facing, BlockState facingState) {
        if(post.getBlock() instanceof WoodPostBlock){
            Direction.Axis axis = post.getValue(WoodPostBlock.AXIS);
            if(facing.getAxis() != axis){
                boolean chain = (facingState.getBlock() instanceof ChainBlock &&
                        facingState.getValue(BlockStateProperties.AXIS) == facing.getAxis());
                return post.setValue(WoodPostBlock.CHAINED[facing.ordinal()], chain);
            }
        }
        return null;
    }

    public static boolean isDropInEnabled(){
        return ModuleLoader.INSTANCE.isModuleEnabled(ExpandedItemInteractionsModule.class);
    }

    public static boolean isEnhancedTooltipEnabled(){
        return ModuleLoader.INSTANCE.isModuleEnabled(ExpandedItemInteractionsModule.class);
    }

    public static boolean isTome(Item item) {
        return item instanceof AncientTomeItem;
    }


    public static void registerTooltipComponent() {
        MinecraftForgeClient.registerTooltipComponentFactory(ItemsUtil.InventoryTooltip.class, InventoryTooltipComponent::new);
    }
}
