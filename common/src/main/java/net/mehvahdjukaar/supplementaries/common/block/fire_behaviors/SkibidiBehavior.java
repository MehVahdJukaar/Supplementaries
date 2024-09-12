package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SkibidiBehavior extends SpawnEggBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, int inaccuracy, @Nullable Player owner) {
        EntityType<HatStandEntity> type = ModEntities.HAT_STAND.get();
        try {
            HatStandEntity e = spawnMob(type, level, firePos, firePos, power, stack);
            if (e != null) {
                level.gameEvent(null, GameEvent.ENTITY_PLACE, BlockPos.containing(firePos));
                e.setSkibidi(true, false, null);
                e.setDeltaMovement(0, 0, 0);
                return true;
            }
        } catch (Exception exception) {
            Supplementaries.LOGGER.error("Error while dispensing spawn egg from trapped present at {}", BlockPos.containing(firePos), exception);
        }
        return false;
    }

}
