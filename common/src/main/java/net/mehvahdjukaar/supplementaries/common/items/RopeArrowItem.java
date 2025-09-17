package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RopeArrowItem extends ArrowItem {
    protected final int maxCharges;

    public RopeArrowItem(int maxCharges, Properties builder) {
        super(builder);
        this.maxCharges = maxCharges;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity shooter, @Nullable ItemStack itemStack2) {
        return new RopeArrowEntity(shooter, level, itemStack.copyWithCount(1), itemStack2);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        RopeArrowEntity arrow = new RopeArrowEntity(pos.x(), pos.y(), pos.z(),
                level, stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }

    public static int getRopes(ItemStack stack) {
        return stack.getOrDefault(ModComponents.CHARGES.get(), 0);
    }

    //return remaining
    public static int addRopes(ItemStack stack, int ropes) {
        int current = getRopes(stack);
        int newCharges = Math.min(getRopeCapacity(), current + ropes);
        int remaining = ropes - (newCharges - current);
        stack.set(ModComponents.CHARGES.get(), newCharges);
        return remaining;
    }

    public static int getRopeCapacity() {
        return CommonConfigs.Tools.ROPE_ARROW_CAPACITY.get();
    }

    public static boolean isValidRope(ItemStack stack) {
        return stack.is(ModTags.ROPES);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int charges = getRopes(stack);
        return Mth.clamp(Math.round(charges * 13.0F / (float) maxCharges), 0, 13);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("message.supplementaries.rope_arrow_tooltip", getRopes(stack),
                this.maxCharges));
        if (!MiscUtils.showsHints(tooltipFlag)) return;
        var override = CommonConfigs.getRopeOverride();
        if (override != null) {
            tooltipComponents.add(Component.translatable("message.supplementaries.rope_arrow", override.key().location().toString())
                    .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        }
    }

    //TODO check
    @Override
    public boolean overrideStackedOnOther(ItemStack ropeArrow, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) {
            ItemStack itemstack = pSlot.getItem();
            if (isValidRope(itemstack)) {
                float ropes = getRopes(ropeArrow);
                int missingRope = (int) (ropeArrow.getMaxDamage() - ropes);
                if (missingRope != 0) {
                    ItemStack ropeTaken = pSlot.safeTake(itemstack.getCount(), missingRope, pPlayer);
                    int remainingRopes = addRopes(ropeArrow, ropeTaken.getCount());

                    this.playInsertSound(pPlayer);

                    ItemStack remaining = ropeTaken.copy();
                    remaining.setCount(remainingRopes);
                    if (!remaining.isEmpty()) {
                        ItemStack rest = pSlot.safeInsert(remaining);
                        if (!rest.isEmpty()) {
                            pPlayer.drop(rest, false);
                        }
                    }
                    return true;
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
