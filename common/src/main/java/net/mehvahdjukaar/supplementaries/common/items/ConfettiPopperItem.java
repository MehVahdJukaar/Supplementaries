package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.common.items.components.ConfettiColors;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ClientReceivers;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfettiPopperItem extends BlockItem {

    public ConfettiPopperItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return (context.isSecondaryUseActive() || (context.canPlace() &&
                context.getLevel().getBlockState(context.getClickedPos()).is(this.getBlock())))
                && super.canPlace(context, state);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, InteractionHand hand) {
        VibeChecker.assertSameLevel(level, player);
        ItemStack heldItem = player.getItemInHand(hand);



        //no clue why im doing this from server side
        Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(0.2)).add(0d, -0.25, 0d);
        //hack
        float oldRot = player.getXRot();
        player.setXRot((float) (oldRot - 20 * Math.cos(oldRot * Mth.DEG_TO_RAD)));
        Vec3 dir = player.getLookAngle();
        ClientBoundParticlePacket packet = getConfettiPacket(heldItem, pos, dir);
        player.setXRot(oldRot);
        if (!level.isClientSide) {
            NetworkHelper.sendToAllClientPlayersTrackingEntity(player, packet);

            level.gameEvent(player, GameEvent.EXPLODE, player.position());
        } else {
            //play immediately for client
            ClientReceivers.spawnConfettiParticles(packet, level, level.random);
        }

        heldItem.consume(1, player);
        return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
    }

    public static @NotNull ClientBoundParticlePacket getConfettiPacket(ItemStack heldItem, Vec3 pos, Vec3 dir) {
        ConfettiColors colorsComp = heldItem.get(ModComponents.CONFETTI_COLORS.get());
        int[] colors;
        if (colorsComp != null) {
            colors = colorsComp.getColors().stream().mapToInt(i -> i).toArray();
        } else {
            colors = new int[]{};
        }
        return new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Kind.CONFETTI,
                dir, colors);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand usedHand) {
        if (entity.getType() == EntityType.CREEPER && entity instanceof Creeper c &&
                entity instanceof IPartyCreeper pc &&
                !c.isIgnited() && !pc.supplementaries$isFestive()) {
            pc.supplementaries$setFestive(true);
            stack.consume(1, player);
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.interactLivingEntity(stack, player, entity, usedHand);
    }

    @ForgeOverride
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ConfettiColors colorsComp = stack.get(ModComponents.CONFETTI_COLORS.get());
        if (colorsComp != null) {
            colorsComp.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }
}
