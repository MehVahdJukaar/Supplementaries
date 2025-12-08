package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerAccessor {

    @Invoker("setShoulderEntityRight")
    void invokeSetShoulderEntityRight(CompoundTag entityCompound);

    @Invoker("setShoulderEntityLeft")
    void invokeSetShoulderEntityLeft(CompoundTag entityCompound);
}
