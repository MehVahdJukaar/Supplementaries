package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.common.misc.IExplorationFunctionExtension;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplorationMapFunction.class)
public abstract class ExplorationMapFunctionMixin implements IExplorationFunctionExtension {
    @Unique
    @Nullable
    ResourceLocation supplementaries$customDecoration = null;

    @Shadow
    @Final
    TagKey<Structure> destination;

    @Shadow
    @Final
    byte zoom;

    @Shadow
    @Final
    MapDecoration.Type mapDecoration;

    @Shadow
    @Final
    int searchRadius;

    @Shadow
    @Final
    boolean skipKnownStructures;

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"), cancellable = true)
    public void turnToQuill(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir, @Local Vec3 pos,
                            @Local ServerLevel level) {
        if (supplementaries$customDecoration != null ||
                (CompatHandler.QUARK && CommonConfigs.Tweaks.REPLACE_VANILLA_MAPS.get())) {
            var targets = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .getTag(destination).orElse(null);

            cir.setReturnValue(AdventurerMapsHandler.createMapOrQuill(context.getLevel(),
                    BlockPos.containing(pos), targets,
                this.searchRadius, this.skipKnownStructures, this.zoom, supplementaries$customDecoration,
                    null, 0));
        }
    }

    @Override
    public ResourceLocation supplementaries$getCustomDecoration() {
        return supplementaries$customDecoration;
    }

    @Override
    public void supplementaries$setCustomDecoration(ResourceLocation resourceLocation) {
        supplementaries$customDecoration = resourceLocation;
    }
}