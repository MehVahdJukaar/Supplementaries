package net.mehvahdjukaar.supplementaries.compat.quark;


import net.mehvahdjukaar.selene.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.module.DispensersPlaceBlocksModule;
import vazkii.quark.content.building.block.WoodPostBlock;
import vazkii.quark.content.tools.item.AncientTomeItem;

public class QuarkPlugin {
    private static final ResourceLocation SACK_CAP = new ResourceLocation(Supplementaries.MOD_ID, "sack_drop_in");
    private static final ResourceLocation SAFE_CAP = new ResourceLocation(Supplementaries.MOD_ID, "safe_drop_in");

    private static final ResourceLocation JAR_CAP = new ResourceLocation(Supplementaries.MOD_ID, "jar_drop_in");

    public static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        Item i = event.getObject().getItem();
        if(i == ModRegistry.SACK_ITEM.get())
            event.addCapability(SACK_CAP, new SackDropIn());
        else if (i == ModRegistry.SAFE_ITEM.get())
            event.addCapability(SAFE_CAP, new SafeDropIn());
        //else if (i instanceof JarItem || i instanceof EmptyJarItem)
        //    event.addCapability(JAR_CAP, new JarDropIn());
    }

    public static boolean hasQButtonOnRight(){
        return GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton;
    }

    //re registers block placement dispenser behaviors (so people wont complain for missing placement behavior on my page lol)
    public static void addMissingDispenserBlockPlacingBehaviors(){

        for(QuarkModule m : ModuleCategory.AUTOMATION.getOwnedModules()){
            if(m instanceof DispensersPlaceBlocksModule){
                DispensersPlaceBlocksModule.BlockBehaviour behavior = new DispensersPlaceBlocksModule.BlockBehaviour();
                for(Item i : DispenserBlock.DISPENSER_REGISTRY.keySet()){
                    //find block items with no registered block placing
                    if(i instanceof BlockItem && !(DispenserBlock.DISPENSER_REGISTRY.get(i)
                            instanceof DispensersPlaceBlocksModule.BlockBehaviour)){


                        //use my dumb block placement thing cause i'm lazy. wont matter for normal blocks
                        DispenserHelper.registerPlaceBlockBehavior(i);
                    }
                }
                break;
            }
        }
    }

    //this should have been implemented in the post block updateShape method
    public static @Nullable BlockState updateWoodPostShape(BlockState post, Direction facing, BlockState facingState){
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

    public static boolean isTome(Item item){
        return item instanceof AncientTomeItem;
    }


}
