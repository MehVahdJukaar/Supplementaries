package net.mehvahdjukaar.supplementaries.common.entities.goals;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nullable;
import java.util.List;

public class ShowWaresGoal extends LookAtPlayerGoal {
    protected final AbstractVillager villager;
    private Player player;
    private final int minDuration;
    private final int maxDuration;
    private final List<ItemStack> displayItems = Lists.newArrayList();
    @Nullable
    private ItemStack playerItemStack;

    private int cycleCounter;
    private int displayIndex;

    private int lookTime;

    public ShowWaresGoal(AbstractVillager mob, int minDuration, int maxDuration) {
        super(mob, Player.class, 8.0F);
        this.villager = mob;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean canUse() {
        if (!this.villager.isTrading()) {
            return super.canUse();
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        } else if(this.villager.isTrading()){
            return false;
        } else if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    @Override
    public void start() {
        this.lookTime = 40;
        this.cycleCounter = 0;
        this.displayIndex = 0;
        this.player = (Player) this.lookAt;
    }

    @Override
    public void tick() {

        this.findItemsToDisplay();
        if (!this.displayItems.isEmpty()) {
            this.displayCyclingItems();
        } else {
            this.villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            //this.lookTime = Math.min(this.lookTime, 40);
        }
        --this.lookTime;
        super.tick();
    }

    @Override
    public void stop() {
        super.stop();
        villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.playerItemStack = null;
    }

    private void findItemsToDisplay() {
        boolean flag = false;
        ItemStack handStack = this.player.getMainHandItem();
        if (this.playerItemStack == null || !ItemStack.isSame(this.playerItemStack, handStack)) {
            this.playerItemStack = handStack;
            flag = true;
            this.displayItems.clear();
        }

        if (flag && !this.playerItemStack.isEmpty()) {
            this.updateDisplayItems();
            if (!this.displayItems.isEmpty()) {
                this.lookTime = 900;
                this.displayFirstItem();
            }
        }

    }

    private void displayFirstItem() {
        this.villager.setItemSlot(EquipmentSlot.MAINHAND, this.displayItems.get(0));
    }

    private void updateDisplayItems() {
        for(MerchantOffer merchantoffer : villager.getOffers()) {
            if (!merchantoffer.isOutOfStock() && this.playerItemStackMatchesCostOfOffer(merchantoffer)) {
                this.displayItems.add(merchantoffer.getResult());
            }
        }
    }

    private boolean playerItemStackMatchesCostOfOffer(MerchantOffer offer) {
        return ItemStack.isSame(this.playerItemStack, offer.getCostA()) || ItemStack.isSame(this.playerItemStack, offer.getCostB());
    }


    private void displayCyclingItems() {
        if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
            ++this.displayIndex;
            this.cycleCounter = 0;
            if (this.displayIndex > this.displayItems.size() - 1) {
                this.displayIndex = 0;
            }

            mob.setItemSlot(EquipmentSlot.MAINHAND, this.displayItems.get(this.displayIndex));
        }

    }


}
