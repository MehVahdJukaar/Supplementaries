package net.mehvahdjukaar.supplementaries.gui;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class NoticeBoardContainer extends Container implements Supplier<Map<Integer, Slot>> {
    public World world;
    public PlayerEntity entity;
    public int x, y, z;
    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;
    public NoticeBoardContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
        super(Registry.NOTICE_BOARD_CONTAINER.get(), id);
        this.entity = inv.player;
        this.world = inv.player.world;
        this.internal = new ItemStackHandler(1);
        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
        if (pos != null) {
            if (extraData.readableBytes() == 1) { // bound to item
                byte hand = extraData.readByte();
                ItemStack itemstack;
                if (hand == 0)
                    itemstack = this.entity.getHeldItemMainhand();
                else
                    itemstack = this.entity.getHeldItemOffhand();
                itemstack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                    this.internal = capability;
                    this.bound = true;
                });
            } else if (extraData.readableBytes() > 1) {
                extraData.readByte(); // drop padding
                Entity entity = world.getEntityByID(extraData.readVarInt());
                if (entity != null)
                    entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
            } else { // might be bound to block
                TileEntity ent = inv.player.world.getTileEntity(pos);
                if (ent != null) {
                    ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
                }
            }
        }
        this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 79, 39) {
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                //NoticeBoardContainer.this.slotChanged(0, 0, 0);
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return (stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK || stack.getItem() instanceof FilledMapItem);
            }
        }));
        int si;
        int sj;
        for (si = 0; si < 3; ++si)
            for (sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 8 + si * 18, 142));
    }

    public Map<Integer, Slot> get() {
        return customSlots;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 1) {
                if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                if (index < 1 + 27) {
                    if (!this.mergeItemStack(itemstack1, 1 + 27, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.mergeItemStack(itemstack1, 1, 1 + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }
                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                if (slot.isItemValid(itemstack) && !itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.putStack(itemstack);
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.putStack(itemstack);
                        flag = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }
            while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();
                if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                    if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.split(slot1.getSlotStackLimit()));
                    } else {
                        slot1.putStack(stack.split(stack.getCount()));
                    }
                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return flag;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        if (!bound && (playerIn instanceof ServerPlayerEntity)) {
            if (!playerIn.isAlive() || ((ServerPlayerEntity) playerIn).hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    playerIn.dropItem(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    playerIn.inventory.placeItemBackInInventory(playerIn.world,
                            internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }
/*
    private void slotChanged(int slotid, int ctype, int meta) {
        if (this.world != null && this.world.isRemote) {
            ModdymcmodfaceMod.PACKET_HANDLER.sendToServer(new GUISlotChangedMessage(slotid, x, y, z, ctype, meta));
            handleSlotAction(entity, slotid, ctype, meta, x, y, z);
        }
    }*/
}

