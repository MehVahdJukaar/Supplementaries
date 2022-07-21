package net.mehvahdjukaar.supplementaries.common.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

public class WrenchItem extends Item {
    private final float attackDamage;
    /**
     * Modifiers applied when the item is in the mainland of a user.
     */
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public WrenchItem(Properties pProperties) {
        super(pProperties);

        this.attackDamage = (float) 2.5;
        float pAttackSpeedModifier = -2f;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", this.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", pAttackSpeedModifier, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public float getDamage() {
        return this.attackDamage;
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.is(Items.COPPER_INGOT);
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pAttacker.level.playSound(null, pTarget, SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 0.5F, 1.8F);

        pStack.hurtAndBreak(1, pAttacker, (entity) -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }


    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }

    //rotate stuff
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();


        if (player != null) { // player.mayUseItemAt()
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            ItemStack itemstack = context.getItemInHand();
            Direction dir = context.getClickedFace();

            boolean shiftDown = player.isShiftKeyDown(); //^ (dir == Direction.DOWN)

            Optional<Direction> success = BlockUtil.tryRotatingBlockAndConnected(dir, shiftDown, pos, level, context.getClickLocation());
            if (success.isPresent()) {
                dir = success.get();

                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, itemstack);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                } else {
                    //only plays them on local client. might want to change this
                    playTurningEffects(pos, shiftDown, dir, level, player);
                }
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                if (level.isClientSide) {
                    level.playSound(context.getPlayer(), player, SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 1.4F, 0.8F);
                }
            }
        }
        return InteractionResult.FAIL;
    }

    public static void playTurningEffects(BlockPos pos, boolean shiftDown, Direction dir, Level level, Player player) {
        if (ClientConfigs.Items.WRENCH_PARTICLES.get()) {
            if (dir == Direction.DOWN) shiftDown = !shiftDown;
            level.addParticle(ModParticles.ROTATION_TRAIL_EMITTER.get(),
                    pos.getX() + 0.5D, pos.getY() + 0.5, pos.getZ() + 0.5D,
                    dir.get3DDataValue(),
                    0.71, shiftDown ? 1 : -1);
        }
        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
        level.playSound(player, player, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.4F);

    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand pUsedHand) {
        if (entity instanceof ArmorStand || entity.getType().is(ModTags.ROTATABLE)) {
            boolean shiftDown = player.isShiftKeyDown();
            float inc = 22.5f * (shiftDown ? -1 : 1);
            entity.setYRot(entity.getYRot() + inc);

            if (player.level.isClientSide)
                playTurningEffects(entity.getOnPos().above(), shiftDown, Direction.UP, player.level, player);

            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pUsedHand));
            return InteractionResult.sidedSuccess(player.level.isClientSide);
        }
        return InteractionResult.PASS;
    }

}
