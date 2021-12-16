package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.AshLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    //fixing a vanilla bug that causes log spam when a block that can provide a tile doesn't actually provide it (they can do this now)
    @Redirect(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean removeBlock(Level level, BlockPos pPos, boolean isMoving) {
        if (!AshLayerBlock.tryConvertToAsh(level, pPos)) level.removeBlock(pPos, isMoving);
        return true;
    }


}