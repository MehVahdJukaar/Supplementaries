package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

public class SkibidiBehavior extends SpawnEggBehavior{

    @Override
    public Optional<ItemStack> useItem(BlockSource source, ItemStack stack) {
        // ALSO FIX HEAD PLACEMENT ON SLABS
        EntityType<HatStandEntity> type = ModEntities.HAT_STAND.get();
        try {
            ServerLevel level = source.getLevel();

            BlockPos pos = source.getPos();
            HatStandEntity e = spawnMob(type, level, source, stack);
            if (e != null) {
                stack.shrink(1);
                level.gameEvent(null, GameEvent.ENTITY_PLACE, pos);
                e.setSkibidi(true, false, null);
                e.setDeltaMovement(0,0,0);
                return Optional.of(stack);
            }
        } catch (Exception exception) {
            Supplementaries.LOGGER.error("Error while dispensing spawn egg from trapped present at {}", source.getPos(), exception);
        }
        return Optional.empty();
    }
}
