package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public interface SlotReference extends Supplier<ItemStack> {




    default Item getItem() {
        return this.get().getItem();
    }

    SlotReference EMPTY = () -> ItemStack.EMPTY;

    static SlotReference hand(Player player, InteractionHand pUsedHand) {
        return () -> player.getItemInHand(pUsedHand);
    }

    static SlotReference slot(LivingEntity entity, EquipmentSlot equipmentSlot) {
        return () -> entity.getItemBySlot(equipmentSlot);
    }

    static SlotReference cont(Inventory inv, int i) {
        return () -> inv.getItem(i);
    }

    static SlotReference inv(Player player, int invSlot) {
        return () -> player.getSlot(invSlot).get();
    }

    static SlotReference empty() {
        return EMPTY;
    }

   default boolean isEmpty(){
        return this != EMPTY;
   };

    ;
}
