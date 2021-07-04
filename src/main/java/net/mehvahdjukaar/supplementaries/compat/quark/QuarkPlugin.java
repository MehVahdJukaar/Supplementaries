package net.mehvahdjukaar.supplementaries.compat.quark;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import vazkii.quark.base.handler.GeneralConfig;

public class QuarkPlugin {
    private static final ResourceLocation SACK_CAP = new ResourceLocation(Supplementaries.MOD_ID, "sack_drop_in");
    private static final ResourceLocation SAFE_CAP = new ResourceLocation(Supplementaries.MOD_ID, "safe_drop_in");


    public static void attachSackDropIn(AttachCapabilitiesEvent<ItemStack> event) {
        Item i = event.getObject().getItem();
        if(i == Registry.SACK_ITEM.get())
            event.addCapability(SACK_CAP, new SackDropIn());
        else if (i == Registry.SAFE_ITEM.get())
            event.addCapability(SAFE_CAP, new SafeDropIn());
    }

    public static boolean hasQButtonOnRight(){
        return GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton;
    }




}
