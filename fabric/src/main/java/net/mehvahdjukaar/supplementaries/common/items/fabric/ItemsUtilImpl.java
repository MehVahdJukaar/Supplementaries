package net.mehvahdjukaar.supplementaries.common.items.fabric;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemsUtilImpl {
    @javax.annotation.Nullable
    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
    }

    @javax.annotation.Nullable
    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
    }

    public static int getAllSacksInInventory(ItemStack stack, Entity entityIn, ServerPlayer player, int amount) {
    }

    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
    }

    public static boolean faucetSpillItems(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
    }
}
