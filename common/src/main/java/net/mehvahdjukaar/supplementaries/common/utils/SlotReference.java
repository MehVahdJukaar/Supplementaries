package net.mehvahdjukaar.supplementaries.common.utils;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.misc.CodecMapRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
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

    Codec<? extends SlotReference> getCodec();

    record Hand(InteractionHand hand) implements SlotReference {

        public static final Codec<Hand> CODEC = Codec.BOOL.xmap(b -> b ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, h -> h == InteractionHand.MAIN_HAND)
                .xmap(Hand::new, Hand::hand);

        @Override
        public ItemStack get(LivingEntity player) {
            return player.getItemInHand(hand);
        }

        @Override
        public Codec<Hand> getCodec() {
            return CODEC;
        }
    }

    record Inv(int invSlot) implements SlotReference {

        public static final Codec<Inv> CODEC = Codec.INT.xmap(Inv::new, Inv::invSlot);

        @Override
        public ItemStack get(LivingEntity player) {
            return player.getSlot(invSlot).get();
        }

        @Override
        public Codec<Inv> getCodec() {
            return CODEC;
        }
    }

    record EqSlot(EquipmentSlot slot) implements SlotReference {
        public static final Codec<EqSlot> CODEC = Codec.INT.xmap(i -> EquipmentSlot.values()[0], Enum::ordinal)
                .xmap(EqSlot::new, EqSlot::slot);

        @Override
        public ItemStack get(LivingEntity player) {
            return player.getItemBySlot(slot);
        }

        @Override
        public Codec<EqSlot> getCodec() {
            return CODEC;
        }
    }

    record Empty() implements SlotReference {
        public static final Codec<Empty> CODEC = Codec.unit(EMPTY);

        @Override
        public ItemStack get(LivingEntity player) {
            return ItemStack.EMPTY;
        }

        @Override
        public Codec<Empty> getCodec() {
            return CODEC;
        }
    }

    record Quiver() implements SlotReference {

        private static final Quiver INSTANCE = new Quiver();
        private static final Codec<Quiver> CODEC = Codec.unit(INSTANCE);

        @Override
        public ItemStack get(LivingEntity player) {
            if (player instanceof Player p) {
                return QuiverItem.findActiveQuiver(p);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public Codec<Quiver> getCodec() {
            return CODEC;
        }
    }


    CodecMapRegistry<SlotReference> REGISTRY = Util.make(() -> {
        CodecMapRegistry<SlotReference> m = new CodecMapRegistry<>("slot_reference");
        m.register("hand", Hand.CODEC);
        m.register("inv", Inv.CODEC);
        m.register("empty", Empty.CODEC);
        m.register("eq_slot", EqSlot.CODEC);
        m.register("quiver", Quiver.CODEC);
        return m;
    });

    Codec<SlotReference> STREAM_CODEC = REGISTRY
            .dispatch(SlotReference::getCodec, c -> c);

    static SlotReference decode(FriendlyByteBuf buf) {
        return STREAM_CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(
                false, e -> Supplementaries.error("Failed to decode slot reference: " + e)
        ).getFirst();
    }


    static void encode(FriendlyByteBuf buf, SlotReference slotReference) {
        buf.writeNbt((CompoundTag) STREAM_CODEC.encodeStart(NbtOps.INSTANCE, slotReference).getOrThrow(
                false, e -> Supplementaries.error("Failed to encode slot reference: " + e)
        ));
    }


}
