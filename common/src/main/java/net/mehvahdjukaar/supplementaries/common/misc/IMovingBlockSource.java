package net.mehvahdjukaar.supplementaries.common.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IMovingBlockSource {

    void supp$setEntity(@Nullable Entity entity);

    @Nullable
    Entity supp$getEntity();

    static BlockSource create(ServerLevel level, Entity entity, DispenserBlockEntity be) {
        BlockSource source = new BlockSource(level, BlockPos.containing(entity.position()), be.getBlockState(), be);
        ((IMovingBlockSource) (Object) source).supp$setEntity(entity);
        return source;
    }
}
