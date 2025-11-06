package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("canAddPassenger")
    boolean invokeCanAddPassenger(Entity e);

    @Invoker("canRide")
    boolean invokeCanRide(Entity e);


}
