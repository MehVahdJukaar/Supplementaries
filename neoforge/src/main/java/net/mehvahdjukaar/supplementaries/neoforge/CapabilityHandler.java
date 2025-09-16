package net.mehvahdjukaar.supplementaries.neoforge;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CapabilityHandler {


    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Supplementaries.MOD_ID);

    private static final Supplier<AttachmentType<AntiquableAttachment>> ANTIQUABLE_ATTACHMENT = ATTACHMENT_TYPES.register(
            "antique_ink", () -> AttachmentType.builder(() -> new AntiquableAttachment(false))
                    .serialize(AntiquableAttachment.CODEC).build());

    public static void init(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
        bus.addListener(CapabilityHandler::register);
    }


    public static final class AntiquableAttachment implements IAntiquable {
        public static final Codec<AntiquableAttachment> CODEC = Codec.BOOL.xmap(AntiquableAttachment::new, a -> a.on);
        private boolean on;

        public AntiquableAttachment(boolean on) {
            this.on = on;
        }

        @Override
        public boolean supplementaries$isAntique() {
            return on;
        }

        @Override
        public void supplementaries$setAntique(boolean hasInk) {
            this.on = hasInk;
        }

        private static AntiquableAttachment get(BlockEntity signBlockEntity, Void direction) {
            return signBlockEntity.getData(ANTIQUABLE_ATTACHMENT);
        }
    }
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

    public static final BlockCapability<IAntiquable, Void> ANTIQUE_TEXT_CAP = BlockCapability
            .createVoid(Supplementaries.res("antique_ink"), IAntiquable.class);

    public static final BlockCapability<IWashable, @Nullable Direction> WASHABLE_CAP = BlockCapability
            .create(Supplementaries.res("washable"), IWashable.class, Direction.class);

    public static final EntityCapability<ICatchableMob, Void> CATCHABLE_MOB = EntityCapability
            .createVoid(Supplementaries.res("antique_ink"), ICatchableMob.class);

    private static final Map<Class<?>, BaseCapability<?, ?>> TOKENS = Map.of(
            IAntiquable.class, ANTIQUE_TEXT_CAP,
            ICatchableMob.class, CATCHABLE_MOB,
            IWashable.class, WASHABLE_CAP
    );

    public static <T> BaseCapability<?, ?> getToken(Class<T> capClass) {
        return TOKENS.get(capClass);
    }

    public static void register(RegisterCapabilitiesEvent event) {

        //TODO: add more
        event.registerBlockEntity(ANTIQUE_TEXT_CAP, BlockEntityType.SIGN, AntiquableAttachment::get);
        event.registerBlockEntity(ANTIQUE_TEXT_CAP, BlockEntityType.HANGING_SIGN, AntiquableAttachment::get);

        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntities.DISPENSER_MINECART.get(),
                (entity, ctx) -> new InvWrapper(entity));
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, ModEntities.DISPENSER_MINECART.get(),
                (entity, ctx) -> new InvWrapper(entity));

        var nonSided = List.of(
                ModRegistry.PRESENT_TILE.get(),
                ModRegistry.TRAPPED_PRESENT_TILE.get(),
                ModRegistry.SAFE_TILE.get(),
                ModRegistry.SACK_TILE.get(),
                ModRegistry.LUNCH_BASKET_TILE.get()
        );
        for (var type : nonSided) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (container, side) -> new InvWrapper(container));
        }

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModRegistry.CANNON_TILE.get(),
                (sidedContainer, side) -> side == null ? new InvWrapper(sidedContainer) :
                        new SidedInvWrapper(sidedContainer, side));


        //if compat handler computer craft add cap to speaker block

        //TODO: add back
        /*
        event.registerItem(Capabilities.ItemHandler.ITEM, new ICapabilityProvider<>() {
            @Override
            public @Nullable IItemHandler getCapability(ItemStack stack, Void object2) {
                if (stack.getItem() instanceof SelectableContainerItem se) {

                }
                return null;
            }
        }, ModRegistry.LUNCH_BASKET.get(), ModRegistry.QUIVER_ITEM.get());
         */

        //so other mods can find them i guess
        /*
        event.register(ICatchableMob.class);
        event.register(IAntiquable.class);
        event.register(IWashable.class);
        event.register(IQuiverEntity.class);*/
    }
}
