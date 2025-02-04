package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.common.utils.IExplorationFunctionExtension;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Mixin(ExplorationMapFunction.class)
public abstract class ExplorationMapFunctionMixin extends LootItemConditionalFunction implements IExplorationFunctionExtension {
    @Unique
    @Nullable
    Holder<MLMapDecorationType<?, ?>> supplementaries$customDecoration = null;

    @Mutable
    @Shadow
    @Final
    public static MapCodec<ExplorationMapFunction> CODEC;

    @Shadow
    @Final
    public static TagKey<Structure> DEFAULT_DESTINATION;

    @Shadow
    @Final
    public static Holder<MapDecorationType> DEFAULT_DECORATION;

    @Shadow
    @Final
    TagKey<Structure> destination;

    @Shadow
    @Final
    Holder<MapDecorationType> mapDecoration;

    @Shadow
    @Final
    int searchRadius;

    @Shadow
    @Final
    boolean skipKnownStructures;

    @Shadow
    @Final
    byte zoom;

    protected ExplorationMapFunctionMixin(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"), cancellable = true)
    public void supp$turnToQuill(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir, @Local Vec3 pos,
                            @Local ServerLevel level) {
        if (supplementaries$customDecoration != null ||
                (CompatHandler.QUARK && CommonConfigs.Tweaks.REPLACE_VANILLA_MAPS.get())) {
            var targets = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .getTag(destination).orElse(null);

            ResourceLocation marker;
            if (supplementaries$customDecoration != null) {
                marker = ResourceLocation.parse(supplementaries$customDecoration.getRegisteredName());
            } else if (mapDecoration == DEFAULT_DECORATION && !destination.equals(StructureTags.ON_WOODLAND_EXPLORER_MAPS)) {
                marker = null; //auto assign a custom marker for structure
            } else {
                marker = ResourceLocation.parse(mapDecoration.getRegisteredName());
            }
            cir.setReturnValue(AdventurerMapsHandler.createMapOrQuill(context.getLevel(),
                    BlockPos.containing(pos), targets,
                    this.searchRadius, this.skipKnownStructures, this.zoom, marker,
                    null, 0));
        }
    }

    @Override
    public Holder<MLMapDecorationType<?, ?>> supplementaries$getCustomDecoration() {
        return supplementaries$customDecoration;
    }

    @Override
    public void supplementaries$setCustomDecoration(Holder<MLMapDecorationType<?, ?>> resourceLocation) {
        supplementaries$customDecoration = resourceLocation;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void supp$modifyCodec(CallbackInfo ci) {
        CODEC = RecordCodecBuilder.mapCodec(
                instance -> commonFields(instance)
                        .and(
                                instance.group(
                                        TagKey.codec(Registries.STRUCTURE)
                                                .optionalFieldOf("destination", DEFAULT_DESTINATION)
                                                .forGetter(explorationMapFunction -> explorationMapFunction.destination),
                                        MapDecorationType.CODEC.optionalFieldOf("decoration", DEFAULT_DECORATION).forGetter(explorationMapFunction -> explorationMapFunction.mapDecoration),
                                        Codec.BYTE.optionalFieldOf("zoom", Byte.valueOf((byte) 2)).forGetter(explorationMapFunction -> explorationMapFunction.zoom),
                                        Codec.INT.optionalFieldOf("search_radius", Integer.valueOf(50)).forGetter(explorationMapFunction -> explorationMapFunction.searchRadius),
                                        Codec.BOOL
                                                .optionalFieldOf("skip_existing_chunks", Boolean.valueOf(true))
                                                .forGetter(explorationMapFunction -> explorationMapFunction.skipKnownStructures),
                                        MLMapDecorationType.CODEC.optionalFieldOf("custom_decoration")
                                                .forGetter(explorationMapFunction ->
                                                        Optional.ofNullable(((IExplorationFunctionExtension) explorationMapFunction).supplementaries$getCustomDecoration()))
                                )
                        )
                        .apply(instance, (lootItemConditions, structureTagKey, mapDecorationTypeHolder, aByte, integer, aBoolean, customDeco) -> {
                            var value = new ExplorationMapFunction(lootItemConditions, structureTagKey, mapDecorationTypeHolder, aByte, integer, aBoolean);
                            if (value instanceof IExplorationFunctionExtension e) {
                                customDeco.ifPresent(e::supplementaries$setCustomDecoration);
                            }
                            return value;
                        })
        );
    }
}