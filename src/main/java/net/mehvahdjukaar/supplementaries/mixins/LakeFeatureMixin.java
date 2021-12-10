package net.mehvahdjukaar.supplementaries.mixins;


import net.minecraft.world.level.levelgen.feature.LakeFeature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LakeFeature.class)
public abstract class LakeFeatureMixin {
//
//    //credit to ThelepaticGrunt
//    @Inject(
//            method = "place",
//            at = @At(value = "INVOKE_ASSIGN",
//                    target = "Lnet/minecraft/core/BlockPos;below(I)Lnet/minecraft/core/BlockPos;"),
//            cancellable = true
//    )
//    private void checkForRoadSigns(FeaturePlaceContext<BlockStateConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
//        WorldGenLevel reader = context.level();
//        BlockPos blockPos = context.origin();
//
//        if (reader.startsForFeature(SectionPos.of(blockPos), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
//                reader.startsForFeature(SectionPos.of(blockPos.offset(16,0,0)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
//                reader.startsForFeature(SectionPos.of(blockPos.offset(-16,0,0)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
//                reader.startsForFeature(SectionPos.of(blockPos.offset(0,0,16)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()||
//                reader.startsForFeature(SectionPos.of(blockPos.offset(0,0,-16)), StructureRegistry.WAY_SIGN.get()).findAny().isPresent()) {
//            cir.setReturnValue(false);
//        }
//    }
}