package net.mehvahdjukaar.supplementaries.gui;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;


public class SackContainer extends Container  {
    private World world;
    private PlayerEntity entity;
    private int x, y, z;
    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;
    public final IInventory inventory;
    public SackContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
        this(id, inv, extraData, new Inventory(5));
    }

    public SackContainer(int id, PlayerInventory inv, PacketBuffer extraData, IInventory inventory) {

        super(Registry.SACK_CONTAINER, id);
        //TODO: remove mcreator stuff and figure how how this works
        this.inventory = inventory;
        inventory.openInventory(inv.player);

        this.entity = inv.player;
        this.world = inv.player.world;
        this.internal = new ItemStackHandler(5);
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
                TileEntity ent = inv.player != null ? inv.player.world.getTileEntity(pos) : null;
                if (ent != null) {
                    ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
                }
            }
        }
        this.customSlots.put(0, this.addSlot(new SackSlot(internal, 0, 43, 35) {
        }));
        this.customSlots.put(1, this.addSlot(new SackSlot(internal, 1, 61, 35) {
        }));
        this.customSlots.put(2, this.addSlot(new SackSlot(internal, 2, 79, 35) {
        }));
        this.customSlots.put(3, this.addSlot(new SackSlot(internal, 3, 97, 35) {
        }));
        this.customSlots.put(4, this.addSlot(new SackSlot(internal, 4, 115, 35) {
        }));
        int si;
        int sj;
        for (si = 0; si < 3; ++si)
            for (sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
        for (si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));
    }

    public Map<Integer, Slot> get() {
        return customSlots;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.inventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 5) {
                if (!this.mergeItemStack(itemstack1, 5, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (!this.mergeItemStack(itemstack1, 0, 5, false)) {
                if (index < 5 + 27) {
                    if (!this.mergeItemStack(itemstack1, 5 + 27, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.mergeItemStack(itemstack1, 5, 5 + 27, false)) {
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
        this.inventory.closeInventory(playerIn);
        if (!bound && (playerIn instanceof ServerPlayerEntity)) {
            if (!playerIn.isAlive() || playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity) playerIn).hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    if (j == 0)
                        continue;
                    playerIn.dropItem(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    if (i == 0)
                        continue;
                    playerIn.inventory.placeItemBackInInventory(playerIn.world,
                            internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }

}


