package net.mehvahdjukaar.supplementaries.compat.quark;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import vazkii.quark.base.handler.GeneralConfig;

public class QuarkPlugin {
    private static final ResourceLocation SACK_CAP = new ResourceLocation(Supplementaries.MOD_ID, "sack_drop_in");


    public static void attachSackDropIn(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(SACK_CAP, new SackDropIn());
    }

    public static boolean hasQButtonOnRight(){
        return GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton;
    }




}
