package net.mehvahdjukaar.supplementaries.api;

import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.MobContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CapturedMobInstance<T extends Entity> {
    protected final T entity;
    protected final float containerWidth;
    protected final float containerHeight;

    protected CapturedMobInstance(T entity,float width, float height) {
        this.entity = entity;
        this.containerWidth = width;
        this.containerHeight = height;
    }

    @Nullable
    public T getEntityForRenderer() {
        return entity;
    }

    /**
     * called each tick while inside a container. You can add here particles, sounds or animations
     * Remember to fallback onto super
     *
     * @param world       world
     * @param pos         world position
     * @param entityScale scale that the mob being rendered is at. useful for particles so they can be rendered accordingly
     * @param entityData  actual mob nbt that is stored in the container. Use this only if you want to store permanent data in your entity which will be kept when it will be released from the container.
     */
    public void containerTick(Level world, BlockPos pos, float entityScale, CompoundTag entityData) {
    }


    /**
     * called when a player right-clicks a container block that contains this mob. Use this to make the player interact with the mob itself.
     * can be used, for example, to make the player trade with the mob in case it was a villager
     *
     * @param world      world
     * @param pos        container block position
     * @param player     player that is interacting
     * @param entityData actual mob nbt that is stored in the container. Use this only if you want to store permanent data in your entity which will be kept when it will be released from the container.
     * @return Pass to do nothing. Success or Consume to prevent any further action
     */
    public InteractionResult onPlayerInteract(Level world, BlockPos pos, Player player, InteractionHand hand, CompoundTag entityData) {
        return InteractionResult.PASS;
    }


    /**
     * use this to react to the container being waterlogged. useful for water mobs to be set out of water
     *
     * @param waterlogged new block waterlogged state
     */
    public void onContainerWaterlogged(boolean waterlogged) {
        if (entity instanceof WaterAnimal && this.entity.isInWater() != waterlogged) {
            entity.wasTouchingWater = waterlogged;
            Pair<Float, Float> dim = MobContainer.calculateMobDimensionsForContainer(this.entity,
                    this.containerWidth, this.containerHeight, waterlogged);
            double py = dim.getRight() + 0.0001;
            entity.setPos(entity.getX(), py, entity.getZ());
            entity.yOld = py;
        }
    }


    //below methods are only used for rendering


    public static class Default<T extends Entity> extends CapturedMobInstance<T> {

        public Default(T entity, float width, float height) {
            super(entity, width, height);
        }
    }
}
