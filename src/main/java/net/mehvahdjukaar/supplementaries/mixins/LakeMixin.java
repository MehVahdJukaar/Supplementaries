package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.world.structures.StructureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LakeFeature.class)
public abstract class LakeMixin {

    //credit to ThelepaticGrunt
    @Inject(
            method = "place",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/BlockPos;below(I)Lnet/minecraft/util/math/BlockPos;"),
            cancellable = true
    )
    private void checkForRoadSigns(WorldGenLevel reader, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateConfiguration singleStateFeatureConfig, CallbackInfoReturnable<Boolean> cir) {
        if (reader.startsForFeature(SectionPos.of(blockPos), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
                reader.startsForFeature(SectionPos.of(blockPos.offset(16,0,0)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
                reader.startsForFeature(SectionPos.of(blockPos.offset(-16,0,0)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
                reader.startsForFeature(SectionPos.of(blockPos.offset(0,0,16)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
                reader.startsForFeature(SectionPos.of(blockPos.offset(0,0,-16)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()) {
            cir.setReturnValue(false);
        }
    }
}