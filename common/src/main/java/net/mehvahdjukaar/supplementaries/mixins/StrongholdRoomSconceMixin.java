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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StrongholdPieces.RoomCrossing.class)
public abstract class StrongholdRoomSconceMixin extends StructurePiece {

    @Final
    @Shadow
    protected int type;

    @Unique
    private final BlockState sconce = ModRegistry.SCONCE_WALL.get().defaultBlockState();

    protected StrongholdRoomSconceMixin(StructurePieceType pType, int pGenDepth, BoundingBox pBoundingBox) {
        super(pType, pGenDepth, pBoundingBox);
    }

    @Inject(method = "postProcess", at = @At("TAIL"))
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource randomSource, BoundingBox bb, ChunkPos chunkPos, BlockPos pos, CallbackInfo ci) {
        if (this.type == 0 && CommonConfigs.Building.SCONCE_ENABLED.get()) {
            this.placeBlock(level, sconce.setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, bb);
            this.placeBlock(level, sconce.setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, bb);
            this.placeBlock(level, sconce.setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, bb);
            this.placeBlock(level, sconce.setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, bb);
        }

    }
}