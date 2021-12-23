package net.mehvahdjukaar.supplementaries.common.items;

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

    public static boolean tryAddingItemInContainerItem(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, boolean inSlot){
        if (action != ClickAction.PRIMARY) {
            if (!incoming.isEmpty() && ItemsUtil.interactWithItemHandler(player, stack, incoming, slot, true, inSlot) != null) {
                ItemStack finished = ItemsUtil.interactWithItemHandler(player, stack, incoming, slot, false, inSlot);
                if (finished != null) {
                    if(inSlot) {
                        slot.set(finished);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack interactWithItemHandler(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean useCopy) {
        if (slot.mayPickup(player)) {

            CompoundTag tag = containerStack.getOrCreateTag();
            CompoundTag cmp = tag.getCompound("BlockEntityTag");
            if (!cmp.contains("LootTable")) {
                BlockEntity te = loadBlockEntityFromItem(cmp.copy(), containerStack.getItem());

                if (te != null) {
                    if (te instanceof SafeBlockTile safe && !safe.canPlayerOpen(player, false)) return null;
                    LazyOptional<IItemHandler> handlerHolder = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if (handlerHolder.isPresent()) {
                        IItemHandler handler = handlerHolder.orElseGet(EmptyHandler::new);
                        ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
                        boolean success = result.isEmpty() || result.getCount() != stack.getCount();
                        if (success) {
                            ItemStack newStack = useCopy ? containerStack.copy() : containerStack;
                            if (!simulate) {
                                stack.setCount(result.getCount());
                            }
                            CompoundTag newTag = new CompoundTag();
                            newTag.put("BlockEntityTag", te.saveWithoutMetadata());
                            newStack.setTag(newTag);
                            if (slot.mayPlace(newStack)) {
                                return newStack;
                            }
                        }
                    }
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
