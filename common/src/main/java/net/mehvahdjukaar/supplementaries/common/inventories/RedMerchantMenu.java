package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
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
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class RedMerchantMenu extends AbstractContainerMenu {

    private final Merchant trader;
    private final MerchantContainer tradeContainer;
    private int merchantLevel;
    private boolean showProgressBar;
    private boolean canRestock;

    public RedMerchantMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id,playerInventory);
    }

    public RedMerchantMenu(int i, Inventory inventory) {
        this(i, inventory, new ClientSideMerchant(inventory.player));
    }

    public RedMerchantMenu(int i, Inventory inventory, Merchant merchant) {
        super(ModMenuTypes.RED_MERCHANT.get(), i);
        this.trader = merchant;
        this.tradeContainer = new MerchantContainer(merchant);
        this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
        this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
        this.addSlot(new MerchantResultSlot(inventory.player, merchant, this.tradeContainer, 2, 220, 37));

        int j;
        for(j = 0; j < 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9 + 9, 108 + k * 18, 84 + j * 18));
            }
        }

        for(j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j, 108 + j * 18, 142));
        }

    }

    public void setShowProgressBar(boolean showProgressBar) {
        this.showProgressBar = showProgressBar;
    }

    @Override
    public void slotsChanged(Container container) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(container);
    }

    public void setSelectionHint(int currentRecipeIndex) {
        this.tradeContainer.setSelectionHint(currentRecipeIndex);
    }

    public boolean stillValid(Player player) {
        return this.trader.getTradingPlayer() == player;
    }

    public int getTraderXp() {
        return this.trader.getVillagerXp();
    }

    public int getFutureTraderXp() {
        return this.tradeContainer.getFutureXp();
    }

    public void setXp(int xp) {
        this.trader.overrideXp(xp);
    }

    public int getTraderLevel() {
        return this.merchantLevel;
    }

    public void setMerchantLevel(int level) {
        this.merchantLevel = level;
    }

    public void setCanRestock(boolean canRestock) {
        this.canRestock = canRestock;
    }

    public boolean canRestock() {
        return this.canRestock;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
                this.playTradeSound();
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    private void playTradeSound() {
        if (!this.trader.isClientSide()) {
            Entity entity = (Entity)this.trader;
            entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }

    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.trader.setTradingPlayer(null);
        if (!this.trader.isClientSide()) {
            if (!player.isAlive() || player instanceof ServerPlayer sp && sp.hasDisconnected()) {
                ItemStack itemStack = this.tradeContainer.removeItemNoUpdate(0);
                if (!itemStack.isEmpty()) {
                    player.drop(itemStack, false);
                }

                itemStack = this.tradeContainer.removeItemNoUpdate(1);
                if (!itemStack.isEmpty()) {
                    player.drop(itemStack, false);
                }
            } else if (player instanceof ServerPlayer) {
                player.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(0));
                player.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(1));
            }

        }
    }

    public void tryMoveItems(int selectedMerchantRecipe) {
        if (selectedMerchantRecipe >= 0 && this.getOffers().size() > selectedMerchantRecipe) {
            ItemStack itemStack = this.tradeContainer.getItem(0);
            if (!itemStack.isEmpty()) {
                if (!this.moveItemStackTo(itemStack, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(0, itemStack);
            }

            ItemStack itemStack2 = this.tradeContainer.getItem(1);
            if (!itemStack2.isEmpty()) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(1, itemStack2);
            }

            if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
                ItemStack itemStack3 = (this.getOffers().get(selectedMerchantRecipe)).getCostA();
                this.moveFromInventoryToPaymentSlot(0, itemStack3);
                ItemStack itemStack4 = (this.getOffers().get(selectedMerchantRecipe)).getCostB();
                this.moveFromInventoryToPaymentSlot(1, itemStack4);
            }

        }
    }

    private void moveFromInventoryToPaymentSlot(int paymentSlotIndex, ItemStack paymentSlot) {
        if (!paymentSlot.isEmpty()) {
            for(int i = 3; i < 39; ++i) {
                ItemStack itemStack = (this.slots.get(i)).getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameTags(paymentSlot, itemStack)) {
                    ItemStack itemStack2 = this.tradeContainer.getItem(paymentSlotIndex);
                    int j = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
                    int k = Math.min(paymentSlot.getMaxStackSize() - j, itemStack.getCount());
                    ItemStack itemStack3 = itemStack.copy();
                    int l = j + k;
                    itemStack.shrink(k);
                    itemStack3.setCount(l);
                    this.tradeContainer.setItem(paymentSlotIndex, itemStack3);
                    if (l >= paymentSlot.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }

    }

    /**
     * {@link net.minecraft.client.multiplayer.ClientPacketListener} uses this to set offers for the client side MerchantContainer.
     */
    public void setOffers(MerchantOffers offers) {
        this.trader.overrideOffers(offers);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}
