package net.mehvahdjukaar.supplementaries.common.capabilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CapabilityHandler {

    private static final Map<Class<?>, Capability<?>> TOKENS = new Object2ObjectOpenHashMap<>();
    public static final Capability<ICatchableMob> CATCHABLE_MOB_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IAntiqueTextProvider> ANTIQUE_TEXT_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ISoapWashable> SOAP_WASHABLE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});


    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ICatchableMob.class);
        event.register(IAntiqueTextProvider.class);
        event.register(ISoapWashable.class);

        TOKENS.put(ICatchableMob.class, CATCHABLE_MOB_CAP);
        TOKENS.put(IAntiqueTextProvider.class, ANTIQUE_TEXT_CAP);
        TOKENS.put(ISoapWashable.class, SOAP_WASHABLE_CAPABILITY);
    }

    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (AntiqueInkHelper.isEnabled() && event.getObject() instanceof SignBlockEntity) {
            event.addCapability(Supplementaries.res("antique_ink"), new AntiqueInkProvider());
        }
    }

    @Nullable
    public static <T> Capability<T> getToken(Class<T> capClass) {
       return (Capability<T>) TOKENS.get(capClass);
    }

    @SuppressWarnings("ConstantConditions")
    @javax.annotation.Nullable
    public static <T> T get(ICapabilityProvider provider, Capability<T> cap){
        return provider.getCapability(cap).orElse(null);
    }
}
