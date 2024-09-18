package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockAccessor {

    @Invoker("tryCatchFire")
    void invokeTryCatchFire(Level level, BlockPos pos, int k, RandomSource ran, int age, Direction face);

}
