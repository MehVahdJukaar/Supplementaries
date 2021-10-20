package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.world.structures.StructureRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.LakesFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LakesFeature.class)
public abstract class LakeMixin {

    //credit to ThelepaticGrunt
    @Inject(
            method = "place",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/BlockPos;below(I)Lnet/minecraft/util/math/BlockPos;"),
            cancellable = true
    )
    private void checkForRoadSigns(ISeedReader reader, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateFeatureConfig singleStateFeatureConfig, CallbackInfoReturnable<Boolean> cir) {
        if (reader.startsForFeature(SectionPos.of(blockPos), StructureRegistry.WAY_SIGN.get()).findAny().isPresent() ||
                reader.startsForFeature(SectionPos.of(blockPos.offset(16, 0, 0)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent() ||
                reader.startsForFeature(SectionPos.of(blockPos.offset(-16, 0, 0)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent() ||
                reader.startsForFeature(SectionPos.of(blockPos.offset(0, 0, 16)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent() ||
                reader.startsForFeature(SectionPos.of(blockPos.offset(0, 0, -16)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()) {
            cir.setReturnValue(false);
        }
    }
}