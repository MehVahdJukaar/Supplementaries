package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.neoforge.QuiverItemImpl;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class CapabilityHandler {

    public static final Capability<ICatchableMob> CATCHABLE_MOB_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IAntiquable> ANTIQUE_TEXT_CAP = CapabilityManager.get(new CapabilityToken<>() {
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

        event.registerItem(Capabilities.ItemHandler.ITEM, new ICapabilityProvider<>() {
            @Override
            public @Nullable IItemHandler getCapability(ItemStack stack, Void object2) {
                if (stack.getItem() instanceof SelectableContainerItem se) {
                    return (IItemHandler) se.getComponentType(stack);
                }
                return null;
            }
        }, ModRegistry.LUNCH_BASKET.get(), ModRegistry.QUIVER_ITEM.get());

        //so other mods can find them i guess
        event.register(ICatchableMob.class);
        event.register(IAntiquable.class);
        event.register(IWashable.class);
        event.register(IQuiverEntity.class);

        TOKENS.put(ICatchableMob.class, CATCHABLE_MOB_CAP);
        TOKENS.put(IAntiquable.class, ANTIQUE_TEXT_CAP);
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
