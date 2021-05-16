package net.mehvahdjukaar.supplementaries.compat.quark;

import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.content.management.capability.ShulkerBoxDropIn;

public class QuarkTooltipPlugin {
    private static final ResourceLocation SHULKER_BOX_CAP = new ResourceLocation("quark", "shulker_box_drop_in");

    //TODO: rewrite all of this and piston stuff to properly use quark api, see charm
    public static void init(){
        GeneralConfig.shulkerBoxes.add(Registry.SACK_ITEM.get().getRegistryName().toString());
    }

    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof SackItem) {
            event.addCapability(SHULKER_BOX_CAP, new ShulkerBoxDropIn());
        }
    }

    public static boolean hasQButtonOnRight(){
        return GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton;
    }

}
