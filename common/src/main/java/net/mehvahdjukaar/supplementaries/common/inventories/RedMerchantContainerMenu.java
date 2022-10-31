package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

public class RedMerchantContainerMenu extends AbstractContainerMenu {
    private final Merchant trader;
    private final MerchantContainer tradeContainer;

    private int merchantLevel;

    private boolean showProgressBar;

    private boolean canRestock;

    public RedMerchantContainerMenu(int i, Inventory inventory) {
        this(i, inventory, new ClientSideMerchant(inventory.player));
    }

    public RedMerchantContainerMenu(int i1, Inventory inventory, Merchant merchant) {
        super(ModRegistry.RED_MERCHANT_CONTAINER.get(), i1);
        this.trader = merchant;
        this.tradeContainer = new MerchantContainer(merchant);
        this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
        this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
        this.addSlot(new MerchantResultSlot(inventory.player, merchant, this.tradeContainer, 2, 220, 37));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 108 + k * 18, 142));
        }
    }

    public RedMerchantContainerMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory);
    }

    public void setShowProgressBar(boolean b) {
        this.showProgressBar = b;
    }

    @Override
    public void slotsChanged(Container pInventory) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(pInventory);
    }

    public void setSelectionHint(int i) {
        this.tradeContainer.setSelectionHint(i);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.trader.getTradingPlayer() == pPlayer;
    }

    public int getTraderXp() {
        return this.trader.getVillagerXp();
    }

    public int getFutureTraderXp() {
        return this.tradeContainer.getFutureXp();
    }

    public void setXp(int i) {
        this.trader.overrideXp(i);
    }

    public int getTraderLevel() {
        return this.merchantLevel;
    }

    public void setMerchantLevel(int i) {
        this.merchantLevel = i;
    }

    public void setCanRestock(boolean b) {
        this.canRestock = b;
    }

    public boolean canRestock() {
        return this.canRestock;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
                this.playTradeSound();
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    private void playTradeSound() {
        if (!this.trader.isClientSide()) {
            Entity entity = (Entity) this.trader;
            entity.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }

    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.trader.setTradingPlayer(null);
        if (!this.trader.isClientSide()) {
            if (!pPlayer.isAlive() || pPlayer instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected()) {
                ItemStack itemstack = this.tradeContainer.removeItemNoUpdate(0);
                if (!itemstack.isEmpty()) {
                    pPlayer.drop(itemstack, false);
                }

                itemstack = this.tradeContainer.removeItemNoUpdate(1);
                if (!itemstack.isEmpty()) {
                    pPlayer.drop(itemstack, false);
                }
            } else {
                pPlayer.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(0));
                pPlayer.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(1));
            }

        }
    }

    public void tryMoveItems(int i) {
        if (this.getOffers().size() > i) {
            ItemStack itemstack = this.tradeContainer.getItem(0);
            if (!itemstack.isEmpty()) {
                if (!this.moveItemStackTo(itemstack, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(0, itemstack);
            }

            ItemStack itemstack1 = this.tradeContainer.getItem(1);
            if (!itemstack1.isEmpty()) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(1, itemstack1);
            }

            if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
                ItemStack itemstack2 = this.getOffers().get(i).getCostA();
                this.moveFromInventoryToPaymentSlot(0, itemstack2);
                ItemStack itemstack3 = this.getOffers().get(i).getCostB();
                this.moveFromInventoryToPaymentSlot(1, itemstack3);
            }

        }
    }

    private void moveFromInventoryToPaymentSlot(int i1, ItemStack stack) {
        if (!stack.isEmpty()) {
            for (int i = 3; i < 39; ++i) {
                ItemStack itemstack = this.slots.get(i).getItem();
                if (!itemstack.isEmpty() && this.isSameItem(stack, itemstack)) {
                    ItemStack itemstack1 = this.tradeContainer.getItem(i1);
                    int j = itemstack1.isEmpty() ? 0 : itemstack1.getCount();
                    int k = Math.min(stack.getMaxStackSize() - j, itemstack.getCount());
                    ItemStack itemstack2 = itemstack.copy();
                    int l = j + k;
                    itemstack.shrink(k);
                    itemstack2.setCount(l);
                    this.tradeContainer.setItem(i1, itemstack2);
                    if (l >= stack.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }

    }

    private boolean isSameItem(ItemStack stack, ItemStack stack1) {
        return stack.getItem() == stack1.getItem() && ItemStack.tagMatches(stack, stack1);
    }

    public void setOffers(MerchantOffers merchantOffers) {
        this.trader.overrideOffers(merchantOffers);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}