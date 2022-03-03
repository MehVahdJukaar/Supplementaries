package net.mehvahdjukaar.supplementaries.common.world.generation;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ModConfiguredStructureFeatures {

    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> CONFIGURED_WAY_SIGN_KEY = makeKey("village_desert");

    public static final Holder<StructureTemplatePool> WAY_SIGN_START =
            Pools.register(new StructureTemplatePool(
                            new ResourceLocation("village/desert/town_centers"),
                            new ResourceLocation("empty"),
                            ImmutableList.of(
                                    Pair.of(StructurePoolElement.legacy("village/desert/town_centers/desert_meeting_point_1"), 98),
                                    Pair.of(StructurePoolElement.legacy("village/desert/town_centers/desert_meeting_point_2"), 98),
                                    Pair.of(StructurePoolElement.legacy("village/desert/town_centers/desert_meeting_point_3"), 49),
                                    Pair.of(StructurePoolElement.legacy("village/desert/zombie/town_centers/desert_meeting_point_1", ProcessorLists.ZOMBIE_DESERT), 2),
                                    Pair.of(StructurePoolElement.legacy("village/desert/zombie/town_centers/desert_meeting_point_2", ProcessorLists.ZOMBIE_DESERT), 2),
                                    Pair.of(StructurePoolElement.legacy("village/desert/zombie/town_centers/desert_meeting_point_3", ProcessorLists.ZOMBIE_DESERT), 1)),
                    StructureTemplatePool.Projection.RIGID));


    public static final Holder<ConfiguredStructureFeature<?, ?>> CONFIGURED_WAY_SIGN_STRUCTURE =
            register(CONFIGURED_WAY_SIGN_KEY, ModStructures.WAY_SIGN.get()
                    .configured(new JigsawConfiguration(DesertVillagePools.START, 3), //max depth
                            BiomeTags.HAS_IGLOO));


    private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> Holder<ConfiguredStructureFeature<?, ?>> register(
            ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey,
            ConfiguredStructureFeature<FC, F> configuredStructureFeature) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, resourceKey, configuredStructureFeature);
    }

    private static ResourceKey<ConfiguredStructureFeature<?, ?>> makeKey(String name) {
        return ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, Supplementaries.res(name));
    }

}
