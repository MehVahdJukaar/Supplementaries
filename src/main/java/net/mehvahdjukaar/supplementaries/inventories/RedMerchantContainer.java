package net.mehvahdjukaar.supplementaries.inventories;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedMerchantContainer extends AbstractContainerMenu {
    private final Merchant trader;
    private final MerchantContainer tradeContainer;

    private int merchantLevel;

    private boolean showProgressBar;

    private boolean canRestock;

    public RedMerchantContainer(int i, Inventory inventory) {
        this(i, inventory, new ClientSideMerchant(inventory.player));
    }

    public RedMerchantContainer(int i1, Inventory inventory, Merchant merchant) {
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

    public RedMerchantContainer(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory);
    }

    public void setShowProgressBar(boolean b) {
        this.showProgressBar = b;
    }

    public void slotsChanged(Container pInventory) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(pInventory);
    }

    public void setSelectionHint(int i) {
        this.tradeContainer.setSelectionHint(i);
    }

    public boolean stillValid(Player pPlayer) {
        return this.trader.getTradingPlayer() == pPlayer;
    }

    @OnlyIn(Dist.CLIENT)
    public int getTraderXp() {
        return this.trader.getVillagerXp();
    }

    @OnlyIn(Dist.CLIENT)
    public int getFutureTraderXp() {
        return this.tradeContainer.getFutureXp();
    }

    @OnlyIn(Dist.CLIENT)
    public void setXp(int i) {
        this.trader.overrideXp(i);
    }

    @OnlyIn(Dist.CLIENT)
    public int getTraderLevel() {
        return this.merchantLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public void setMerchantLevel(int i) {
        this.merchantLevel = i;
    }

    @OnlyIn(Dist.CLIENT)
    public void setCanRestock(boolean b) {
        this.canRestock = b;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canRestock() {
        return this.canRestock;
    }

    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return false;
    }

    public ItemStack quickMoveStack(Player p_82846_1_, int p_82846_2_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_82846_2_);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (p_82846_2_ == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
                this.playTradeSound();
            } else if (p_82846_2_ != 0 && p_82846_2_ != 1) {
                if (p_82846_2_ >= 3 && p_82846_2_ < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (p_82846_2_ >= 30 && p_82846_2_ < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
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

            slot.onTake(p_82846_1_, itemstack1);
        }

        return itemstack;
    }

    private void playTradeSound() {
        if (!this.trader.getLevel().isClientSide) {
            Entity entity = (Entity) this.trader;
            this.trader.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }

    }

    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.trader.setTradingPlayer(null);
        if (!this.trader.getLevel().isClientSide) {
            if (!pPlayer.isAlive() || pPlayer instanceof ServerPlayer && ((ServerPlayer) pPlayer).hasDisconnected()) {
                ItemStack itemstack = this.tradeContainer.removeItemNoUpdate(0);
                if (!itemstack.isEmpty()) {
                    pPlayer.drop(itemstack, false);
                }

                itemstack = this.tradeContainer.removeItemNoUpdate(1);
                if (!itemstack.isEmpty()) {
                    pPlayer.drop(itemstack, false);
                }
            } else {
                pPlayer.getInventory().placeItemBackInInventory(pPlayer.level, this.tradeContainer.removeItemNoUpdate(0));
                pPlayer.getInventory().placeItemBackInInventory(pPlayer.level, this.tradeContainer.removeItemNoUpdate(1));
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

    @OnlyIn(Dist.CLIENT)
    public void setOffers(MerchantOffers merchantOffers) {
        this.trader.overrideOffers(merchantOffers);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}