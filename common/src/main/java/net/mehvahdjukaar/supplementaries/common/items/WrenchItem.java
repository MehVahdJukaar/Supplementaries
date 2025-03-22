package net.mehvahdjukaar.supplementaries.common.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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

    @ForgeOverride
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
        pAttacker.level().playSound(null, pTarget,
                ModSounds.WRENCH_HIT.get(), pAttacker.getSoundSource(), 1,
                0.9f + pAttacker.getRandom().nextFloat() * 0.2f);

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

    //called both from here and from event just to be sure
    //rotate stuff
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player != null) { // player.mayUseItemAt()
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (CompatHandler.FLAN && !FlanCompat.canPlace(player, pos)) return InteractionResult.FAIL;

            ItemStack itemstack = context.getItemInHand();
            Direction dir = context.getClickedFace();

            boolean shiftDown = player.isShiftKeyDown(); //^ (dir == Direction.DOWN)

            Optional<Direction> success = BlockUtil.tryRotatingBlockAndConnected(dir, shiftDown, pos, level, context.getClickLocation());
            if (success.isPresent()) {
                dir = success.get();

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemstack);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                }
                playTurningEffects(pos, shiftDown, dir, level, player);
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                level.playSound(context.getPlayer(), player,
                        ModSounds.WRENCH_FAIL.get(), SoundSource.PLAYERS, 1.4F, 0.8F);
            }
        }
        return InteractionResult.FAIL;
    }

    public static void playTurningEffects(BlockPos pos, boolean shiftDown, Direction dir, Level level, Player player) {
        if (!level.isClientSide) {
            if (dir == Direction.DOWN) {
                dir = dir.getOpposite();
            }
            if (shiftDown) dir = dir.getOpposite();
            ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntityAndSelf(player,
                    new ClientBoundParticlePacket(pos.getCenter(), ClientBoundParticlePacket.Type.WRENCH_ROTATION,
                            dir.get3DDataValue()));
        }
        //called for both so we play sound immediately here
        level.playSound(player, pos, ModSounds.BLOCK_ROTATE.get(), SoundSource.BLOCKS, 1.0F, 1);
        level.playSound(player, player, ModSounds.WRENCH_ROTATE.get(), SoundSource.PLAYERS, 1.0F, 1.4F);
    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand pUsedHand) {
        if (entity instanceof ArmorStand || entity.getType().is(ModTags.ROTATABLE)) {
            boolean shiftDown = player.isShiftKeyDown();
            float inc = 22.5f * (shiftDown ? -1 : 1);
            entity.setYRot(entity.getYRot() + inc);

            Level level = player.level();
            if (level.isClientSide)
                playTurningEffects(entity.getOnPos().above(), shiftDown, Direction.UP, level, player);

            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pUsedHand));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return super.use(level, player, usedHand);
    }
}
