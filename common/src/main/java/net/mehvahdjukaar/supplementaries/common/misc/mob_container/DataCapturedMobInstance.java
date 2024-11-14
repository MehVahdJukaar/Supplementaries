package net.mehvahdjukaar.supplementaries.common.misc.mob_container;

import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
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

    protected DataCapturedMobInstance(T entity, DataDefinedCatchableMob type) {
        super(entity);
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
    public void onContainerWaterlogged(boolean waterlogged, float containerWidth, float containerHeight) {
        var f = this.properties.renderFluid.orElse(null);
        if (!waterlogged && f != null && MLBuiltinSoftFluids.WATER.is(f)) {
           super.onContainerWaterlogged(true, containerWidth, containerHeight);
        } else super.onContainerWaterlogged(waterlogged, containerWidth, containerHeight);
    }


}
