package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container;

import net.mehvahdjukaar.supplementaries.api.CapturedMobInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

//instance
public class DataCapturedMobInstance<T extends Entity> extends CapturedMobInstance<T> {

    private final DataDefinedCatchableMob properties;

    @Nullable
    private final BuiltinAnimation<T> builtinAnimationInstance;

    protected DataCapturedMobInstance(T entity, float width, float height, DataDefinedCatchableMob type) {
        super(entity, width, height);
        this.properties = type;
        this.builtinAnimationInstance = BuiltinAnimation.get(entity, properties.builtinAnimation);
    }

    @Override
    public void containerTick(Level world, BlockPos pos, float entityScale, CompoundTag entityData) {
        entity.tickCount++;
        if (properties.tickMode.isValid(world) && entity instanceof LivingEntity livingEntity) {
            livingEntity.aiStep();
        }

        if (world instanceof ServerLevel serverLevel) {
            properties.loot.ifPresent(lootParam -> lootParam.tryDropping(serverLevel, pos, entity));
        }
        if (builtinAnimationInstance != null) builtinAnimationInstance.tick(entity, world, pos);
    }

    //force water check
    @Override
    public void onContainerWaterlogged(boolean waterlogged) {
        var f = this.properties.forceFluidID.orElse(null);
        if (f != null && f.getPath().equals("water")) {
            onContainerWaterlogged(true);
        } else super.onContainerWaterlogged(waterlogged);
    }


}
