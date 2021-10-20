package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AgeableEntity.class)
public abstract class AgeableEntityMixin extends CreatureEntity {


    protected AgeableEntityMixin(EntityType<? extends CreatureEntity> p_i48575_1_, World p_i48575_2_) {
        super(p_i48575_1_, p_i48575_2_);
    }

    @Shadow
    public abstract void ageUp(int age, boolean forced);

    @Shadow
    public abstract int getAge();

    @Override
    public void ate() {
        if (this.isBaby()) {
            this.ageUp((int) ((float) (-this.getAge() / 20) * 0.1F), true);
        }
    }
}