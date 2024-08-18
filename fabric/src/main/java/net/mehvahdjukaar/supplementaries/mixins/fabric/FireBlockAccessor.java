package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockAccessor {

    @Invoker("canBurn")
    boolean invokeCanBurn(BlockState state);

    @Invoker("checkBurnOut")
    void invokeCheckBurnOut(Level level, BlockPos pos, int chance, RandomSource random, int age);

}
