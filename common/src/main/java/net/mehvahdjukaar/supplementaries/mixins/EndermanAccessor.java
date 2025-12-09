package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnderMan.class)
public interface EndermanAccessor {

    @Invoker("teleport")
    boolean invokeTeleport();
}
