package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.worldgen.MineshaftElevatorPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MineshaftPieces.MineShaftCorridor.class)
public abstract class MineshaftCorridorMixin {

    @Shadow
    private static void fillColumnBetween(WorldGenLevel level, BlockState state, BlockPos.MutableBlockPos pos, int minY, int maxY) {
    }

    @Inject(method = "fillColumnBetween", at = @At("HEAD"), cancellable = true)
    private static void addRope(WorldGenLevel level, BlockState state, BlockPos.MutableBlockPos pos, int minY, int maxY, CallbackInfo ci) {
        if(state.getBlock() == Blocks.CHAIN && minY > MineshaftElevatorPiece.getRopeCutout()){
            BlockState ropeState = MineshaftElevatorPiece.getMineshaftRope();
            if(ropeState != null) {
                ci.cancel();
                fillColumnBetween(level, ropeState, pos, minY, maxY);
            }
        }
    }

}
