package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.world.generation.MineshaftElevatorPiece;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MineshaftPieces.class)
public class MineshaftPiecesMixin {

    @Inject(method = "createRandomShaftPiece", at = @At("HEAD"), cancellable = true)
    private static void addElevator(StructurePieceAccessor pieces, RandomSource random, int x, int y, int z, Direction direction, int genDepth, MineshaftStructure.Type type, CallbackInfoReturnable<MineshaftPieces.MineShaftPiece> cir) {
        var elevator = MineshaftElevatorPiece.getElevator(pieces, random, x, y,z, direction, genDepth, type);
        if (elevator != null) cir.setReturnValue(elevator);
    }
}
