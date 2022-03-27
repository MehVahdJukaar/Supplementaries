package net.mehvahdjukaar.supplementaries.common.world.generation;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ModConfiguredStructureFeatures {

    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> CONFIGURED_WAY_SIGN_KEY = makeKey("way_sign");

    public static final Holder<StructureTemplatePool> WAY_SIGN_START =
            Pools.register(new StructureTemplatePool(
                    Supplementaries.res("way_sign/start_pool"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(
                            StructurePoolElement.legacy("supplementaries:way_sign"), 1)),
                    StructureTemplatePool.Projection.RIGID));


    public static final Holder<ConfiguredStructureFeature<?, ?>> CONFIGURED_WAY_SIGN_STRUCTURE =
            register(CONFIGURED_WAY_SIGN_KEY, ModStructures.WAY_SIGN.get()
                    .configured(new JigsawConfiguration(WAY_SIGN_START, 4), //max depth
                            ModTags.HAS_WAY_SIGNS, true)); //transform surrounding land


    private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> Holder<ConfiguredStructureFeature<?, ?>> register(
            ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey,
            ConfiguredStructureFeature<FC, F> configuredStructureFeature) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, resourceKey, configuredStructureFeature);
    }

    private static ResourceKey<ConfiguredStructureFeature<?, ?>> makeKey(String name) {
        return ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, Supplementaries.res(name));
    }

}
