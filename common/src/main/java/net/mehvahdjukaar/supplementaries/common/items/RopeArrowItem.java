package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RopeArrowItem extends ArrowItem {
    public RopeArrowItem(Properties builder) {
        super(builder);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        int charges = getRopes(stack);
        return new RopeArrowEntity(world, shooter, charges);
    }

    public static int getRopes(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public static void addRopes(ItemStack stack, int ropes) {
        stack.setDamageValue(stack.getDamageValue() - ropes);
    }

    public static int getRopeCapacity() {
        return CommonConfigs.Tools.ROPE_ARROW_CAPACITY.get();
    }

    public static boolean isValidRope(ItemStack stack) {
        return stack.is(ModTags.ROPES);
    }

    @ForgeOverride
    public int getMaxDamage(ItemStack stack) {
        return getRopeCapacity();
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
        tooltip.add(Component.translatable("message.supplementaries.rope_arrow_tooltip", getRopes(stack), getRopeCapacity()));
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
    public boolean overrideStackedOnOther(ItemStack ropeArrow, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) {
            ItemStack itemstack = pSlot.getItem();
            if (isValidRope(itemstack)) {
                float ropes = getRopes(ropeArrow);
                int missingRope = (int) (ropeArrow.getMaxDamage() - ropes);
                if (missingRope != 0) {
                    ItemStack ropeTaken = pSlot.safeTake(itemstack.getCount(), missingRope, pPlayer);
                    int ropeWeCanAdd = Math.min(missingRope, ropeTaken.getCount());
                    addRopes(ropeArrow, ropeWeCanAdd);
                    this.playInsertSound(pPlayer);
                    return true;
                    //pSlot.set(remaining);
                }
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack ropeArrow, ItemStack ropeStack, Slot pSlot, ClickAction pAction,
                                            Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            if (isValidRope(ropeStack)) {
                float ropes = getRopes(ropeArrow);
                int missingRope = (int) (ropeArrow.getMaxDamage() - ropes);
                if (missingRope != 0) {
                    int ropeWeCanAdd = Math.min(missingRope, ropeStack.getCount());
                    addRopes(ropeArrow, ropeWeCanAdd);
                    ropeStack.shrink(ropeWeCanAdd);
                    this.playInsertSound(pPlayer);
                    return true;
                }
            }
        }
        return false;
    }


    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(ModSounds.ROPE_PLACE.get(), 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
