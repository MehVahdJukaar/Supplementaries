package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractProjectileDispenseBehavior.class)
public interface AbstractProjectileBehaviorAccessor {

    @Invoker("getUncertainty")
    float invokeGetUncertainty();

    @Invoker("getPower")
    float invokeGetPower();

    @Invoker("getProjectile")
    Projectile invokeGetProjectile(Level level, Position position, ItemStack stack);

    @Invoker("playSound")
    void invokePlaySound(BlockSource source);
}
