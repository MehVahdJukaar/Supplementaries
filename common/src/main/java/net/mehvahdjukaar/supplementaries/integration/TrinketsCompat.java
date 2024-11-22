package net.mehvahdjukaar.supplementaries.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompat {

    static KeyLockableTile.KeyStatus getKey(Player player, String password) {

        TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinket != null) {
            var found = trinket.getEquipped(i ->
                    i.is(ModTags.KEYS) || i.getItem() instanceof KeyItem);
            if (found.isEmpty()) return KeyLockableTile.KeyStatus.NO_KEY;
            for (var slot : found) {
                ItemStack stack = slot.getB();
                if (IKeyLockable.getKeyStatus(stack, password).isCorrect()) {
                    return KeyLockableTile.KeyStatus.CORRECT_KEY;
                }
            }
            return KeyLockableTile.KeyStatus.INCORRECT_KEY;
        }
        return IKeyLockable.KeyStatus.NO_KEY;
    }

    static SlotReference getQuiver(Player player) {
        TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinket != null) {
            var found = trinket.getEquipped(ModRegistry.QUIVER_ITEM.get());
            if (!found.isEmpty()) {
                return Trinket.of(found.getFirst().getA(), trinket);
            }
        }
        return SlotReference.EMPTY;
    }

    public record Trinket(String slotName, String groupKey, int id) implements SlotReference {

        public static final StreamCodec<RegistryFriendlyByteBuf, Trinket> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Trinket::slotName,
                ByteBufCodecs.STRING_UTF8, Trinket::groupKey,
                ByteBufCodecs.INT, Trinket::id,
                Trinket::new
        );

        public static Trinket of(dev.emi.trinkets.api.SlotReference ref, TrinketComponent comp) {
            int id = ref.index();
            TrinketInventory inv = ref.inventory();
            SlotType slotType = inv.getSlotType();
            return new Trinket(slotType.getName(), slotType.getGroup(), id);
        }

        @Override
        public ItemStack get(LivingEntity player) {
            TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).orElse(null);
            if (trinket != null) {
                var i = trinket.getInventory().get(groupKey);
                if (i != null) {
                    TrinketInventory inv = i.get(slotName);
                    if (inv != null) {
                        return inv.getItem(id);
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends SlotReference> getCodec() {
            return CODEC;
        }
    }

    static {
        SlotReference.REGISTRY.register("trinket", Trinket.CODEC);
    }
}
