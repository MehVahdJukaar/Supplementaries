package net.mehvahdjukaar.supplementaries.common.mobholder;

import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.minecraft.entity.Entity;

/**
 * based typed implementation
 * @param <T> entity type
 */
public abstract class BaseCatchableMobCap<T extends Entity> implements ICatchableMob {

    protected final T mob;

    public BaseCatchableMobCap(T entity) {
        this.mob = entity;
    }

    @Override
    public Entity getEntity() {
        return this.mob;
    }
}
