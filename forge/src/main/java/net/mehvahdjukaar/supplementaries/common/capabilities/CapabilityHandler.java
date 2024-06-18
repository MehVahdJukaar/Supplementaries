package net.mehvahdjukaar.supplementaries.common.capabilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.items.forge.LunchBoxItemImpl;
import net.mehvahdjukaar.supplementaries.common.items.forge.QuiverItemImpl;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CapabilityHandler {

    private static final Map<Class<?>, Capability<?>> TOKENS = new Object2ObjectOpenHashMap<>();

    public static final Capability<ICatchableMob> CATCHABLE_MOB_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IAntiqueTextProvider> ANTIQUE_TEXT_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IWashable> SOAP_WASHABLE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<QuiverItemImpl.Cap> QUIVER_ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<LunchBoxItemImpl.Cap> LUNCH_BOX_ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IQuiverEntity> QUIVER_PLAYER = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        //so other mods can find them i guess
        event.register(ICatchableMob.class);
        event.register(IAntiqueTextProvider.class);
        event.register(IWashable.class);
        event.register(QuiverItemImpl.Cap.class);
        event.register(LunchBoxItemImpl.Cap.class);
        event.register(IQuiverEntity.class);

        TOKENS.put(ICatchableMob.class, CATCHABLE_MOB_CAP);
        TOKENS.put(IAntiqueTextProvider.class, ANTIQUE_TEXT_CAP);
        TOKENS.put(IWashable.class, SOAP_WASHABLE_CAPABILITY);
        TOKENS.put(QuiverItemImpl.Cap.class, QUIVER_ITEM_HANDLER);
        TOKENS.put(LunchBoxItemImpl.Cap.class, LUNCH_BOX_ITEM_HANDLER);
        TOKENS.put(IQuiverEntity.class, QUIVER_PLAYER);
    }

    public static void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (AntiqueInkItem.isEnabled() && (event.getObject() instanceof SignBlockEntity ||
                event.getObject() instanceof HangingSignBlockEntity)) {
            event.addCapability(Supplementaries.res("antique_ink"), new AntiqueInkProvider());
        }
    }

    @Nullable
    public static <T> Capability<T> getToken(Class<T> capClass) {
        return (Capability<T>) TOKENS.get(capClass);
    }

    @SuppressWarnings("ConstantConditions")
    @org.jetbrains.annotations.Nullable
    public static <T> T get(ICapabilityProvider provider, Capability<T> cap) {
        return provider.getCapability(cap).orElse(null);
    }

    @SuppressWarnings("ConstantConditions")
    @org.jetbrains.annotations.Nullable
    public static <T> T get(ICapabilityProvider provider, Capability<T> cap, Direction dir) {
        return provider.getCapability(cap, dir).orElse(null);
    }


}
