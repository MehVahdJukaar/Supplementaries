package net.mehvahdjukaar.supplementaries.common.capabilities.mobholder;

import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.minecraft.world.entity.Entity;

/**
 * based typed implementation
 * @param <T> entity type
 */
public abstract class BaseCatchableMobCap<T extends Entity> implements ICatchableMob {

    protected final T mob;

    protected float containerWidth = 1;
    protected float containerHeight = 1;

    public BaseCatchableMobCap(T entity) {
        this.mob = entity;
    }

    @Override
    public Entity getEntity() {
        return this.mob;
    }

    @Override
    public void setContainerDimensions(float width, float height) {
        this.containerWidth = width;
        this.containerHeight = height;
    }

    public float getContainerWidth() {
        return containerWidth;
    }

    public float getContainerHeight() {
        return containerHeight;
    }
}
