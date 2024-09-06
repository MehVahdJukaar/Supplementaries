package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ConfettiPopperItem extends Item {

    public ConfettiPopperItem(Properties properties) {
        super(properties);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, InteractionHand hand) {
        if (!level.isClientSide) {

            //no clue why im doing this from server side
            Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(0.2)).add(0d, -0.25, 0d);
            //hack
            float oldRot = player.getXRot();
            player.setXRot((float) (oldRot - 20 * Math.cos(oldRot * Mth.DEG_TO_RAD)));
            ClientBoundParticlePacket packet = new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Type.CONFETTI,
                    null, player.getLookAngle());
            player.setXRot(oldRot);
            ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(level, BlockPos.containing(pos), packet);

            level.gameEvent(player, GameEvent.EXPLODE, player.position());
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand usedHand) {
        if (entity.getType() == EntityType.CREEPER && entity instanceof Creeper c &&
                entity instanceof IPartyCreeper pc &&
                !c.isIgnited() && !pc.supplementaries$isFestive()) {
            pc.supplementaries$setFestive(true);
            if (!player.isCreative()) {
                player.getItemInHand(usedHand).shrink(1);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.interactLivingEntity(stack, player, entity, usedHand);
    }


}
