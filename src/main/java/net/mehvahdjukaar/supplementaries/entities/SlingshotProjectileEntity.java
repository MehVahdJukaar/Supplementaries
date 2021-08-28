package net.mehvahdjukaar.supplementaries.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class SlingshotProjectileEntity extends ProjectileItemEntity {
    public SlingshotProjectileEntity(EntityType<? extends ProjectileItemEntity> p_i50155_1_, World p_i50155_2_) {
        super(p_i50155_1_, p_i50155_2_);
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }
}
