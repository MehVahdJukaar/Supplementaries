package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StrongholdPieces.FiveCrossing.class)
public abstract class StrongholdCrossingSconceMixin extends StructurePiece {

    @Unique
    private final BlockState supplementaries$sconce = ModRegistry.SCONCE_WALL.get().defaultBlockState();

    protected StrongholdCrossingSconceMixin(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
        super(structurePieceType, i, boundingBox);
    }


    @Inject(method = "postProcess", at = @At("TAIL"))
    public void supp$addSconces(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox bb, ChunkPos chunkPos, BlockPos pos, CallbackInfo ci) {
        if (CommonConfigs.Building.SCONCE_ENABLED.get())
            this.placeBlock(level, supplementaries$sconce.setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, bb);
    }
}