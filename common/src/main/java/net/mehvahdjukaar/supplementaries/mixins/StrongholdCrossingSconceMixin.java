package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
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
    private final BlockState sconce = ModRegistry.SCONCE_WALL.get().defaultBlockState();

    protected StrongholdCrossingSconceMixin(StructurePieceType p_209994_, int p_209995_, BoundingBox p_209996_) {
        super(p_209994_, p_209995_, p_209996_);
    }


    @Inject(method = "postProcess", at = @At("TAIL"))
    public void postProcess(WorldGenLevel level, StructureManager p_229535_, ChunkGenerator p_229536_, RandomSource p_229537_, BoundingBox bb, ChunkPos p_229539_, BlockPos p_229540_, CallbackInfo ci) {
        if (RegistryConfigs.HAS_STRONGHOLD_SCONCE)
            this.placeBlock(level, sconce.setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, bb);
    }
}