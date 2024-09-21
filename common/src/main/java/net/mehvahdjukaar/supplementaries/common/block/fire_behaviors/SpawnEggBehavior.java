package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpawnEggBehavior implements IFireItemBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, int inaccuracy, @Nullable Player owner) {
        EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack);
        try {
            Entity e = spawnMob(type, level, firePos, direction, power, stack, owner);
            if (e != null) {
                level.gameEvent(null, GameEvent.ENTITY_PLACE, BlockPos.containing(firePos));
                //update client velocity
               /// NetworkHelper.sendToAllClientPlayersInDefaultRange(level, BlockPos.containing(firePos),
               //         new ClientBoundSendKnockbackPacket(e.getDeltaMovement(), e.getId()));
                return true;
            }
        } catch (Exception exception) {
            Supplementaries.LOGGER.error("Error while dispensing spawn egg from trapped present at {}", BlockPos.containing(firePos), exception);
        }
        return false;
    }

    @Nullable
    protected <T extends Entity> T spawnMob(EntityType<T> entityType, ServerLevel serverLevel,
                                            Vec3 firePos, Vec3 direction, float power, @Nullable ItemStack stack,
                                            @Nullable Player player) {

        // we cant call spawn as we want no sound nor any custom equipment due to difficulty
        T entity = entityType.create(serverLevel);
        if (entity != null) {

            if (stack != null) {
                // adds item stuff to entity
                EntityType.createDefaultStackConfig(serverLevel, stack, player).accept(entity);
            }

            entity.setPos(firePos.x(), firePos.y(), firePos.z());
            entity.moveTo(firePos.x(), firePos.y(), firePos.z(), Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);

            entity.setDeltaMovement(direction.scale(power));
            entity.hasImpulse = true;
            entity.hurtMarked = true;

            if (entity instanceof Mob mob) {
                mob.yHeadRot = mob.getYRot();
                mob.yBodyRot = mob.getYRot();
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                        MobSpawnType.DISPENSER, null);
                mob.playAmbientSound();
            }
            serverLevel.addFreshEntity(entity);
        }
        return entity;
    }


}
