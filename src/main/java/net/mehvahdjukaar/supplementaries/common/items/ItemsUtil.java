package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;

public class ItemsUtil {

    public record InventoryTooltip(CompoundTag tag, Item item, int size) implements TooltipComponent {
    }

    public static boolean tryInteractingWithContainerItem(ItemStack containerStack, ItemStack incoming, Slot slot, ClickAction action, Player player, boolean inSlot) {
        if (action != ClickAction.PRIMARY) {
            //drop content in empty slot
            if (incoming.isEmpty()) {
                if (!inSlot) {
                    return ItemsUtil.removeItemFromHandler(player, containerStack, slot);
                }
            } else if (ItemsUtil.addItemToHandler(player, containerStack, incoming, slot, true, inSlot)) {
                return ItemsUtil.addItemToHandler(player, containerStack, incoming, slot, false, inSlot);
            }
        }
        return false;
    }


    @Nullable
    public static boolean addItemToHandler(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        if (slot.mayPickup(player)) {

            var handlerAndTe = getItemHandler(containerStack, player);
            if (handlerAndTe != null) {
                IItemHandler handler = handlerAndTe.getFirst();
                ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
                boolean success = result.isEmpty() || result.getCount() != stack.getCount();
                if (success) {
                    if (simulate) {
                        return true;
                    } else {
                        //this is a mess and probably not even correct
                        CompoundTag newTag = new CompoundTag();
                        newTag.put("BlockEntityTag", handlerAndTe.getSecond().saveWithoutMetadata());
                        if (inSlot) {
                            stack.setCount(result.getCount());
                            ItemStack newStack = containerStack.copy();
                            if (slot.mayPlace(newStack)) {
                                newStack.setTag(newTag);
                                slot.set(newStack);
                                return true;
                            }
                        } else {
                            int i = stack.getCount() - result.getCount();
                            slot.safeTake( i, i, player);
                            containerStack.setTag(newTag);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static boolean removeItemFromHandler(Player player, ItemStack containerStack, Slot slot) {
        if (slot.mayPickup(player)) {

            var handlerAndTe = getItemHandler(containerStack, player);
            if (handlerAndTe != null) {
                IItemHandler handler = handlerAndTe.getFirst();
                for (int s = 0; s < handler.getSlots(); s++) {
                    ItemStack selected = handler.getStackInSlot(s);
                    if (!selected.isEmpty()) {
                        ItemStack dropped = handler.extractItem(s, 1, false);

                        if (slot.mayPlace(dropped)) {
                            slot.set(dropped);
                            CompoundTag newTag = new CompoundTag();
                            newTag.put("BlockEntityTag", handlerAndTe.getSecond().saveWithoutMetadata());
                            containerStack.setTag(newTag);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }


    @Nullable
    public static Pair<IItemHandler, BlockEntity> getItemHandler(ItemStack containerStack, Player player) {
        CompoundTag tag = containerStack.getOrCreateTag();
        CompoundTag cmp = tag.getCompound("BlockEntityTag");
        if (!cmp.contains("LootTable")) {
            BlockEntity te = loadBlockEntityFromItem(cmp.copy(), containerStack.getItem());

            if (te != null) {
                if (te instanceof SafeBlockTile safe && !safe.canPlayerOpen(player, false)) return null;
                LazyOptional<IItemHandler> handlerHolder = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handlerHolder.isPresent()) {
                    return Pair.of(handlerHolder.orElseGet(EmptyHandler::new), te);
                }
            }
        }
        return null;
    }

    @Nullable
    public static BlockEntity loadBlockEntityFromItem(CompoundTag tag, Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof EntityBlock entityBlock) {
                BlockEntity te = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                if (te != null) te.load(tag);
                return te;
            }
        }
        return null;
    }

}
