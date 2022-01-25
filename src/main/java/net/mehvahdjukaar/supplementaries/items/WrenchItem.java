package net.mehvahdjukaar.supplementaries.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.Optional;

public class WrenchItem extends Item {
    private final float attackDamage;
    /**
     * Modifiers applied when the item is in the mainhand of a user.
     */
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public WrenchItem(Item.Properties pProperties) {
        super(pProperties);

        this.attackDamage = (float) 2.5;
        float pAttackSpeedModifier = -2f;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", this.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", pAttackSpeedModifier, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public float getDamage() {
        return this.attackDamage;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(
                Enchantments.KNOCKBACK).contains(enchantment);
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.getItem() == Items.IRON_INGOT;
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pAttacker.level.playSound(null, pTarget, SoundEvents.ANVIL_PLACE, SoundCategory.NEUTRAL, 0.5F, 1.8F);

        pStack.hurtAndBreak(1, pAttacker, (entity) -> entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
        return true;
    }


    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlotType.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }

    //rotate stuff
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();


        if (player != null) { // player.mayUseItemAt()
            World level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            ItemStack itemstack = context.getItemInHand();
            Direction dir = context.getClickedFace();

            boolean shiftDown = player.isShiftKeyDown(); //^ (dir == Direction.DOWN)

            Optional<Direction> success = BlockUtils.tryRotatingBlockAndConnected(dir, shiftDown, pos, level, context.getClickLocation());
            if (success.isPresent()) {
                dir = success.get();

                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, itemstack);
                    //level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                } else {
                    playTurningEffects(pos, shiftDown, dir, level, player);
                }
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));

                return ActionResultType.sidedSuccess(level.isClientSide);
            } else {
                if (level.isClientSide) {
                    //level.playSound(context.getPlayer(), player, SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 1.4F, 0.8F);
                }
            }
        }
        return ActionResultType.FAIL;
    }

    private void playTurningEffects(BlockPos pos, boolean shiftDown, Direction dir, World level, PlayerEntity player) {

        if (dir == Direction.DOWN) shiftDown = !shiftDown;
        level.addParticle(ModRegistry.ROTATION_TRAIL_EMITTER.get(),
                pos.getX() + 0.5D, pos.getY() + 0.5, pos.getZ() + 0.5D,
                dir.get3DDataValue(),
                0.71, shiftDown ? 1 : -1);

        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundCategory.BLOCKS, 1.0F, 0.6F);
        //level.playSound(player, player, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.4F);

    }


    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand pUsedHand) {
        if (entity instanceof ArmorStandEntity || entity.getType().getRegistryName().getNamespace().equals("dummmmmmy")) {
            boolean shiftDown = player.isShiftKeyDown();
            float inc = 22.5f * (shiftDown ? -1 : 1);
            entity.yRot = (entity.yRot + inc);

            if (player.level.isClientSide)
                playTurningEffects(entity.blockPosition().above(), shiftDown, Direction.UP, player.level, player);

            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pUsedHand));
            return ActionResultType.sidedSuccess(player.level.isClientSide);
        }
        return ActionResultType.PASS;
    }

    private static Method SET_DIRECTION = null;

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        boolean shiftDown = player.isShiftKeyDown();
        if (entity instanceof HangingEntity) {
            HangingEntity hangingEntity = ((HangingEntity) entity);
            if (hangingEntity.getDirection().getAxis().isHorizontal()) {
                //hangingEntity.rotate(player.isShiftKeyDown() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90);
                Direction dir = hangingEntity.getDirection();
                dir = shiftDown ? dir.getCounterClockWise() : dir.getClockWise();
                try {
                    if (SET_DIRECTION == null)
                        SET_DIRECTION = ObfuscationReflectionHelper.findMethod(HangingEntity.class, "setDirection", Direction.class);
                    SET_DIRECTION.setAccessible(true);
                    SET_DIRECTION.invoke(hangingEntity, dir);

                    if (player.level.isClientSide)
                        playTurningEffects(hangingEntity.getPos(), shiftDown, Direction.UP, player.level, player);

                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    return true;
                } catch (Exception ignored) {
                }
            }
        } else if (entity instanceof ArmorStandEntity) {
            this.interactLivingEntity(stack, player, (LivingEntity) entity, Hand.MAIN_HAND);

            return true;
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
