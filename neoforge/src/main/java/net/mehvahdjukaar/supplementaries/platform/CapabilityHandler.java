package net.mehvahdjukaar.supplementaries.platform;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.platform.platform.ForgeHelperImpl;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.platform.FluidHandlerItemCap;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CapabilityHandler {


    public static final BlockCapability<IAntiquable, Void> ANTIQUE_TEXT_CAP = BlockCapability
            .createVoid(Supplementaries.res("antique_ink"), IAntiquable.class);
    public static final BlockCapability<IWashable, @Nullable Direction> WASHABLE_CAP = BlockCapability
            .create(Supplementaries.res("washable"), IWashable.class, Direction.class);
    public static final EntityCapability<ICatchableMob, Void> CATCHABLE_MOB = EntityCapability
            .createVoid(Supplementaries.res("antique_ink"), ICatchableMob.class);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Supplementaries.MOD_ID);
    /*
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
    });*/
    private static final Supplier<AttachmentType<AntiquableAttachment>> ANTIQUABLE_ATTACHMENT = ATTACHMENT_TYPES.register(
            "antique_ink", () -> AttachmentType.builder(() -> new AntiquableAttachment(false))
                    .serialize(AntiquableAttachment.CODEC).build());
    private static final Map<Class<?>, BaseCapability<?, ?>> TOKENS = Map.of(
            IAntiquable.class, ANTIQUE_TEXT_CAP,
            ICatchableMob.class, CATCHABLE_MOB,
            IWashable.class, WASHABLE_CAP
    );

    public static void init(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
            bus.addListener(CapabilityHandler::register);
    }

    public static <T> BaseCapability<?, ?> getToken(Class<T> capClass) {
        return TOKENS.get(capClass);
    }

    public static void register(RegisterCapabilitiesEvent event) {

        Block[] signBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(SignBlock.class::isInstance)
                .toArray(Block[]::new);
        if (signBlocks.length > 0) {
            event.registerBlock(ANTIQUE_TEXT_CAP, (level, pos, state, be, ctx) -> {
                if (be instanceof SignBlockEntity sign) {
                    return AntiquableAttachment.get(sign, ctx);
                }
                return null;
            }, signBlocks);
        }

        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntities.DISPENSER_MINECART.get(),
                (entity, ctx) -> new InvWrapper(entity));
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, ModEntities.DISPENSER_MINECART.get(),
                (entity, ctx) -> new InvWrapper(entity));

        var nonSided = List.of(
                ModRegistry.PRESENT_TILE.get(),
                ModRegistry.TRAPPED_PRESENT_TILE.get(),
                ModRegistry.SAFE_TILE.get(),
                ModRegistry.SACK_TILE.get(),
                ModRegistry.CANNON_TILE.get(),
                ModRegistry.LUNCH_BASKET_TILE.get()
        );
        for (var type : nonSided) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                    (container, side) ->
                            ForgeHelperImpl.makeDefaultInvHandler((Container) container, side));
            //use this its more correct. otherwise can take item through face won't be called
        }


        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) ->
                new FluidBucketWrapper(stack), ModFluids.LUMISENE_BUCKET.get());

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) ->
                        new FluidHandlerItemCap(stack, 250, Items.GLASS_BOTTLE, ModFluids.LUMISENE_BOTTLE.get(),
                                ModFluids.LUMISENE_FLUID.get()),
                ModFluids.LUMISENE_BOTTLE.get());

        //if compat handler computer craft add cap to speaker block

        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> {
            if (stack.getItem() instanceof SelectableContainerItem<?, ?> se) {
                return new SelectableContainerItemHandler<>(stack, se);
            }
            return null;
        }, ModRegistry.LUNCH_BASKET_ITEM.get(), ModRegistry.QUIVER_ITEM.get());
    }

    private static final class SelectableContainerItemHandler<C extends SelectableContainerContent<M>, M extends SelectableContainerContent.Mut<C>>
            implements IItemHandler {
        private final ItemStack stack;
        private final DataComponentType<C> componentType;

        SelectableContainerItemHandler(ItemStack stack, SelectableContainerItem<C, M> item) {
            this.stack = stack;
            this.componentType = item.getComponentType();
        }

        @Override
        public int getSlots() {
            C c = stack.get(componentType);
            return c != null ? c.getSize() : 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            C c = stack.get(componentType);
            return c != null ? c.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack item, boolean simulate) {
            C c = stack.get(componentType);
            if (c == null) return item;
            M mutable = c.toMutable();
            ItemStack result = mutable.insertItem(slot, item, simulate);
            if (!simulate) stack.set(componentType, mutable.toImmutable());
            return result;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            C c = stack.get(componentType);
            if (c == null) return ItemStack.EMPTY;
            M mutable = c.toMutable();
            ItemStack result = mutable.extractItem(slot, amount, simulate);
            if (!simulate) stack.set(componentType, mutable.toImmutable());
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            C c = stack.get(componentType);
            return c != null ? c.toMutable().getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack item) {
            C c = stack.get(componentType);
            return c != null && c.toMutable().isItemValid(slot, item);
        }
    }

    public static final class AntiquableAttachment implements IAntiquable {
        public static final Codec<AntiquableAttachment> CODEC = Codec.BOOL.xmap(AntiquableAttachment::new, a -> a.on);
        private boolean on;

        public AntiquableAttachment(boolean on) {
            this.on = on;
        }

        private static AntiquableAttachment get(BlockEntity signBlockEntity, Void direction) {
            return signBlockEntity.getData(ANTIQUABLE_ATTACHMENT);
        }

        @Override
        public boolean supplementaries$isAntique() {
            return on;
        }

        @Override
        public void supplementaries$setAntique(boolean hasInk) {
            this.on = hasInk;
        }
    }
}
