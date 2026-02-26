package net.mehvahdjukaar.supplementaries.common.utils;

import io.netty.buffer.ByteBuf;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.codec.CodecUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface SlotReference {

    default Item getItem(LivingEntity player) {
        return this.get(player).getItem();
    }

    ItemStack get(LivingEntity player);

    Empty EMPTY = new Empty();

    static SlotReference hand(InteractionHand pUsedHand) {
        return new Hand(pUsedHand);
    }

    static SlotReference slot(EquipmentSlot equipmentSlot) {
        return new EqSlot(equipmentSlot);
    }

    static SlotReference inv(int invSlot) {
        return new Inv(invSlot);
    }

    static @NotNull SlotReference quiver(IQuiverEntity e) {
        return Quiver.INSTANCE;
    }


    default boolean isEmpty() {
        return this == EMPTY;
    }

    StreamCodec<? super RegistryFriendlyByteBuf, ? extends SlotReference> getCodec();

    record Hand(InteractionHand hand) implements SlotReference {

        public static final StreamCodec<FriendlyByteBuf, Hand> CODEC = CodecUtils.enumStreamCodec(InteractionHand.class)
                .map(Hand::new, Hand::hand);

        @Override
        public ItemStack get(LivingEntity player) {
            return player.getItemInHand(hand);
        }

        @Override
        public StreamCodec<FriendlyByteBuf, Hand> getCodec() {
            return CODEC;
        }
    }

    record Inv(int invSlot) implements SlotReference {

        public static final StreamCodec<ByteBuf, Inv> CODEC = ByteBufCodecs.INT.map(Inv::new, Inv::invSlot);

        @Override
        public ItemStack get(LivingEntity player) {
            return player.getSlot(invSlot).get();
        }

        @Override
        public StreamCodec<ByteBuf, Inv> getCodec() {
            return CODEC;
        }
    }

    record EqSlot(EquipmentSlot slot) implements SlotReference {
        public static final StreamCodec<FriendlyByteBuf, EqSlot> CODEC = CodecUtils.enumStreamCodec(EquipmentSlot.class)
                .map(EqSlot::new, EqSlot::slot);

        @Override
        public ItemStack get(LivingEntity player) {
            return player.getItemBySlot(slot);
        }

        @Override
        public StreamCodec<FriendlyByteBuf, EqSlot> getCodec() {
            return CODEC;
        }
    }

    record Empty() implements SlotReference {
        public static final StreamCodec<ByteBuf, Empty> CODEC = StreamCodec.unit(EMPTY);

        @Override
        public ItemStack get(LivingEntity player) {
            return ItemStack.EMPTY;
        }

        @Override
        public StreamCodec<ByteBuf, Empty> getCodec() {
            return CODEC;
        }
    }

    record Quiver() implements SlotReference {

        private static final Quiver INSTANCE = new Quiver();
        private static final StreamCodec<FriendlyByteBuf, Quiver> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public ItemStack get(LivingEntity player) {
            if (player instanceof Player p) {
                return QuiverItem.findActiveQuiver(p);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public StreamCodec<FriendlyByteBuf, Quiver> getCodec() {
            return CODEC;
        }
    }

    ResourceKey<Registry<StreamCodec<ByteBuf, SlotReference>>> TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Supplementaries.res("slot_reference_type"));

    @SuppressWarnings("all")
    Registry<StreamCodec<ByteBuf, SlotReference>> TYPE_REGISTRY = Util.make(() -> {
        var reg = RegHelper.registerRegistry(TYPE_REGISTRY_KEY, false);
        RegHelper.register(Supplementaries.res("hand"), () -> (StreamCodec) Hand.CODEC, TYPE_REGISTRY_KEY);
        RegHelper.register(Supplementaries.res("inv"), () -> (StreamCodec) Inv.CODEC, TYPE_REGISTRY_KEY);
        RegHelper.register(Supplementaries.res("empty"), () -> (StreamCodec) Empty.CODEC, TYPE_REGISTRY_KEY);
        RegHelper.register(Supplementaries.res("eq_slot"), () -> (StreamCodec) EqSlot.CODEC, TYPE_REGISTRY_KEY);
        RegHelper.register(Supplementaries.res("quiver"), () -> (StreamCodec) Quiver.CODEC, TYPE_REGISTRY_KEY);
        return reg;
    });


    @SuppressWarnings("all")
    StreamCodec<RegistryFriendlyByteBuf, SlotReference> STREAM_CODEC = ByteBufCodecs.registry(TYPE_REGISTRY_KEY)
            .dispatch(o -> (StreamCodec) o.getCodec(), c -> c);


}
