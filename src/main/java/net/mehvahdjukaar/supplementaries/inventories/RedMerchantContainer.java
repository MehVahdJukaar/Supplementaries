package net.mehvahdjukaar.supplementaries.inventories;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedMerchantContainer extends AbstractContainerMenu {
    private final Merchant trader;
    private final MerchantContainer tradeContainer;
    @OnlyIn(Dist.CLIENT)
    private int merchantLevel;
    @OnlyIn(Dist.CLIENT)
    private boolean showProgressBar;
    @OnlyIn(Dist.CLIENT)
    private boolean canRestock;

    public RedMerchantContainer(int p_i50068_1_, Inventory p_i50068_2_) {
        this(p_i50068_1_, p_i50068_2_, new ClientSideMerchant(p_i50068_2_.player));
    }

    public RedMerchantContainer(int p_i50069_1_, Inventory p_i50069_2_, Merchant p_i50069_3_) {
        super(ModRegistry.RED_MERCHANT_CONTAINER.get(), p_i50069_1_);
        this.trader = p_i50069_3_;
        this.tradeContainer = new MerchantContainer(p_i50069_3_);
        this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
        this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
        this.addSlot(new MerchantResultSlot(p_i50069_2_.player, p_i50069_3_, this.tradeContainer, 2, 220, 37));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(p_i50069_2_, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(p_i50069_2_, k, 108 + k * 18, 142));
        }

    }

    public RedMerchantContainer(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i,playerInventory);
    }

    @OnlyIn(Dist.CLIENT)
    public void setShowProgressBar(boolean p_217045_1_) {
        this.showProgressBar = p_217045_1_;
    }

    public void slotsChanged(Container p_75130_1_) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(p_75130_1_);
    }

    public void setSelectionHint(int p_75175_1_) {
        this.tradeContainer.setSelectionHint(p_75175_1_);
    }

    public boolean stillValid(Player p_75145_1_) {
        return this.trader.getTradingPlayer() == p_75145_1_;
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
    public void setXp(int p_217052_1_) {
        this.trader.overrideXp(p_217052_1_);
    }

    @OnlyIn(Dist.CLIENT)
    public int getTraderLevel() {
        return this.merchantLevel;
    }

    @OnlyIn(Dist.CLIENT)
    public void setMerchantLevel(int p_217043_1_) {
        this.merchantLevel = p_217043_1_;
    }

    @OnlyIn(Dist.CLIENT)
    public void setCanRestock(boolean p_223431_1_) {
        this.canRestock = p_223431_1_;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canRestock() {
        return this.canRestock;
    }

    public boolean canTakeItemForPickAll(ItemStack p_94530_1_, Slot p_94530_2_) {
        return false;
    }

    public ItemStack quickMoveStack(Player p_82846_1_, int p_82846_2_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_82846_2_);
        if (slot != null && slot.hasItem()) {
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
            Entity entity = (Entity)this.trader;
            this.trader.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }

    }

    public void removed(Player p_75134_1_) {
        super.removed(p_75134_1_);
        this.trader.setTradingPlayer((Player)null);
        if (!this.trader.getLevel().isClientSide) {
            if (!p_75134_1_.isAlive() || p_75134_1_ instanceof ServerPlayer && ((ServerPlayer)p_75134_1_).hasDisconnected()) {
                ItemStack itemstack = this.tradeContainer.removeItemNoUpdate(0);
                if (!itemstack.isEmpty()) {
                    p_75134_1_.drop(itemstack, false);
                }

                itemstack = this.tradeContainer.removeItemNoUpdate(1);
                if (!itemstack.isEmpty()) {
                    p_75134_1_.drop(itemstack, false);
                }
            } else {
                p_75134_1_.inventory.placeItemBackInInventory(p_75134_1_.level, this.tradeContainer.removeItemNoUpdate(0));
                p_75134_1_.inventory.placeItemBackInInventory(p_75134_1_.level, this.tradeContainer.removeItemNoUpdate(1));
            }

        }
    }

    public void tryMoveItems(int p_217046_1_) {
        if (this.getOffers().size() > p_217046_1_) {
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
                ItemStack itemstack2 = this.getOffers().get(p_217046_1_).getCostA();
                this.moveFromInventoryToPaymentSlot(0, itemstack2);
                ItemStack itemstack3 = this.getOffers().get(p_217046_1_).getCostB();
                this.moveFromInventoryToPaymentSlot(1, itemstack3);
            }

        }
    }

    private void moveFromInventoryToPaymentSlot(int p_217053_1_, ItemStack p_217053_2_) {
        if (!p_217053_2_.isEmpty()) {
            for(int i = 3; i < 39; ++i) {
                ItemStack itemstack = this.slots.get(i).getItem();
                if (!itemstack.isEmpty() && this.isSameItem(p_217053_2_, itemstack)) {
                    ItemStack itemstack1 = this.tradeContainer.getItem(p_217053_1_);
                    int j = itemstack1.isEmpty() ? 0 : itemstack1.getCount();
                    int k = Math.min(p_217053_2_.getMaxStackSize() - j, itemstack.getCount());
                    ItemStack itemstack2 = itemstack.copy();
                    int l = j + k;
                    itemstack.shrink(k);
                    itemstack2.setCount(l);
                    this.tradeContainer.setItem(p_217053_1_, itemstack2);
                    if (l >= p_217053_2_.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }

    }

    private boolean isSameItem(ItemStack p_217050_1_, ItemStack p_217050_2_) {
        return p_217050_1_.getItem() == p_217050_2_.getItem() && ItemStack.tagMatches(p_217050_1_, p_217050_2_);
    }

    @OnlyIn(Dist.CLIENT)
    public void setOffers(MerchantOffers p_217044_1_) {
        this.trader.overrideOffers(p_217044_1_);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}