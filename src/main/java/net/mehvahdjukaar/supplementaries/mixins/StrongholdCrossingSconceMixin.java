package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(StrongholdPieces.FiveCrossing.class)
public abstract class StrongholdCrossingSconceMixin extends StructurePiece {

    @Unique
    private final BlockState sconce = ModRegistry.SCONCE_WALL.get().defaultBlockState();

    protected StrongholdCrossingSconceMixin(StructurePieceType p_209994_, int p_209995_, BoundingBox p_209996_) {
        super(p_209994_, p_209995_, p_209996_);
    }


    @Inject(method = "postProcess", at = @At("TAIL"))
    public void postProcess(WorldGenLevel reader, StructureFeatureManager p_192518_, ChunkGenerator p_192519_, Random p_192520_, BoundingBox bb, ChunkPos p_192522_, BlockPos p_192523_, CallbackInfo ci) {
        if(RegistryConfigs.Reg.HAS_STRONGHOLD_SCONCE)
        this.placeBlock(reader, sconce.setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, bb);
    }
}