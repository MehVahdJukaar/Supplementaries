package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RopeArrowItem extends ArrowItem {
    public RopeArrowItem(Properties builder) {
        super(builder);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        int charges = stack.getMaxDamage() - stack.getDamageValue();
        return new RopeArrowEntity(world, shooter, charges);
    }

    @ForgeOverride
    public int getMaxDamage(ItemStack stack) {
        return CommonConfigs.Tools.ROPE_ARROW_CAPACITY.get();
    }

    @ForgeOverride
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        return false;
    }

    @ForgeOverride
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @ForgeOverride
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - stack.getDamageValue() * 13.0F / this.getMaxDamage(stack));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x6f4c36;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("message.supplementaries.rope_arrow_tooltip", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()));
        if (worldIn == null) return;
        if (!MiscUtils.showsHints(worldIn, flagIn)) return;
        var override = CommonConfigs.getRopeOverride();
        if (override != null) {
            tooltip.add(Component.translatable("message.supplementaries.rope_arrow", override.key().location())
                    .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        }
    }

    //TODO
    @Override
    public boolean overrideStackedOnOther(ItemStack arrow, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) return false;
        float damage = arrow.getDamageValue();
        if (damage == 0) return false;

        ItemStack itemstack = pSlot.getItem();
        //place into slot
        boolean didStuff = false;
        if (!itemstack.isEmpty() && itemstack.getItem() instanceof BlockItem bi && bi.getBlock() == CommonConfigs.getSelectedRope()) {
            var taken = pSlot.safeTake(itemstack.getCount(), itemstack.getMaxStackSize(), pPlayer);
            ItemStack remaining = data.tryAdding(taken);
            if (!remaining.equals(taken)) {
                this.playInsertSound(pPlayer);
                didStuff = true;
            }
            pSlot.set(remaining);
        }
        return didStuff;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack quiver, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            AtomicBoolean didStuff = new AtomicBoolean(false);
            if (!pOther.isEmpty()) {
                ItemStack i = data.tryAdding(pOther);
                if (!i.equals(pOther)) {
                    this.playInsertSound(pPlayer);
                    pAccess.set(i);
                    didStuff.set(true);
                }
            }
            return didStuff.get();
        }
        return false;
    }

    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(ModSounds.ROPE_PLACE.get(), 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
