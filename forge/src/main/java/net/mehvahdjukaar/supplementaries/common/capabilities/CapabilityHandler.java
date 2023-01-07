package net.mehvahdjukaar.supplementaries.common.capabilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.forge.QuiverItemImpl;
import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CapabilityHandler {

    private static final Map<Class<?>, Capability<?>> TOKENS = new Object2ObjectOpenHashMap<>();
    public static final Capability<ICatchableMob> CATCHABLE_MOB_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IAntiqueTextProvider> ANTIQUE_TEXT_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ISoapWashable> SOAP_WASHABLE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<QuiverItemImpl.QuiverCapability> QUIVER_ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IQuiverEntity> QUIVER_PLAYER = CapabilityManager.get(new CapabilityToken<>() {});


    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ICatchableMob.class);
        event.register(IAntiqueTextProvider.class);
        event.register(ISoapWashable.class);

        TOKENS.put(ICatchableMob.class, CATCHABLE_MOB_CAP);
        TOKENS.put(IAntiqueTextProvider.class, ANTIQUE_TEXT_CAP);
        TOKENS.put(ISoapWashable.class, SOAP_WASHABLE_CAPABILITY);
    }

    public static void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (AntiqueInkHelper.isEnabled() && event.getObject() instanceof SignBlockEntity) {
            event.addCapability(Supplementaries.res("antique_ink"), new AntiqueInkProvider());
        }
    }

    public static void attachPlayerCapabilities(AttachCapabilitiesEvent<Player> event) {
        if (RegistryConfigs.QUIVER_ENABLED.get() && event.getObject() instanceof Player) {
         //   event.addCapability(Supplementaries.res("quiver_entity"), new QuiverEntityProvider(event.getObject()));
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
