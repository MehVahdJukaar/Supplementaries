package net.mehvahdjukaar.supplementaries.mixins.forge.self;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.supplementaries.common.items.WrenchItem;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WrenchItem.class)
public abstract class SelfWrenchMixin extends Item {

    public SelfWrenchMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(
                Enchantments.KNOCKBACK).contains(enchantment);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        boolean shiftDown = player.isShiftKeyDown();
        if (entity instanceof HangingEntity hangingEntity && hangingEntity.getDirection().getAxis().isHorizontal()) {
            //hangingEntity.rotate(player.isShiftKeyDown() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90);
            Direction dir = hangingEntity.getDirection();
            dir = shiftDown ? dir.getCounterClockWise() : dir.getClockWise();

            hangingEntity.setDirection(dir);

            if (player.level().isClientSide) {
                WrenchItem.playTurningEffects(hangingEntity.getPos(), shiftDown, Direction.UP, player.level(), player);
            }
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            return true;

        } else if (entity instanceof LivingEntity armorStand) {
           if( this.interactLivingEntity(stack, player, armorStand, InteractionHand.MAIN_HAND).consumesAction()){
               return true;
           }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
