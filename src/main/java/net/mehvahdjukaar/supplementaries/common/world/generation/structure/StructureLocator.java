package net.mehvahdjukaar.supplementaries.common.world.generation.structure;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.mehvahdjukaar.moonlight.math.Vec2i;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
//import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;


public class StructureLocator {

    private static int dist(BlockPos pos1, BlockPos pos2) {
        int i = pos2.getX() - pos1.getX();
        int j = pos2.getZ() - pos1.getZ();
        return (int) (Mth.sqrt((float) (i * i + j * j)));
    }

    @Nullable
    static public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestRandomMapFeature(
            ServerLevel level, TagKey<ConfiguredStructureFeature<?, ?>> tagKey,  BlockPos pos,
            int maximumChunkDistance, boolean newlyGenerated) {
        var found = findNearestMapFeatures(level, tagKey, pos, maximumChunkDistance, newlyGenerated, 1, false);

        if (found.size() > 0) return found.get(0);
        return null;
    }

    static public List<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> findNearestMapFeatures(
            ServerLevel level, TagKey<ConfiguredStructureFeature<?, ?>> tagKey, BlockPos pos,
            int maximumChunkDistance, boolean newlyGenerated, int requiredCount) {
        return findNearestMapFeatures(level, tagKey, pos, maximumChunkDistance, newlyGenerated, requiredCount, false);
    }


    static public List<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> findNearestMapFeatures(
            ServerLevel level, TagKey<ConfiguredStructureFeature<?, ?>> tagKey, BlockPos pos,
            int maximumChunkDistance, boolean newlyGenerated, int requiredCount, boolean selectRandom) {

        List<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> foundStructures = new ArrayList<>();

        if (!level.getServer().getWorldData().worldGenSettings().generateFeatures()) {
            return foundStructures;
        }
        Optional<HolderSet.Named<ConfiguredStructureFeature<?, ?>>> optional = level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).getTag(tagKey);
        if (optional.isEmpty()) return foundStructures;

        var targets = optional.get();

        Set<Holder<Biome>> targetBiomes = targets.stream().flatMap((holder) -> holder.value().biomes().stream()).collect(Collectors.toSet());

        //return early if these features can't spawn in any biome
        if (targetBiomes.isEmpty()) return foundStructures;

        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        Set<Holder<Biome>> possibleBiomes = chunkGenerator.getBiomeSource().possibleBiomes();
        //if they have biomes in common
        if (Collections.disjoint(possibleBiomes, targetBiomes)) return foundStructures;


        double maxDist = Double.MAX_VALUE;

        List<Holder<ConfiguredStructureFeature<?, ?>>> selectedTargets = new ArrayList<>();

        //adds all the structures that can generate
        for (Holder<ConfiguredStructureFeature<?, ?>> holder : targets) {
            if (possibleBiomes.stream().anyMatch(holder.value().biomes()::contains)) {
                selectedTargets.add(holder);
            }
        }

        //for adventure maps
        if (selectRandom) {
            Holder<ConfiguredStructureFeature<?, ?>> selected = selectedTargets.get(level.random.nextInt(selectedTargets.size()));
            selectedTargets = List.of(selected);
        }

        //structures that can generate
        Map<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> reachableTargetsMap = new Object2ObjectArrayMap<>();


        for (var holder : selectedTargets) {
            for (StructurePlacement structureplacement : chunkGenerator.getPlacementsForFeature(holder)) {
                reachableTargetsMap.computeIfAbsent(structureplacement, (placement) -> new ObjectArraySet<>()).add(holder);
            }
        }


        List<Pair<RandomSpreadStructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>>> list = new ArrayList<>(reachableTargetsMap.size());

        int maxSpacing = 0;

        //stronghold hax
        for (Map.Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> entry : reachableTargetsMap.entrySet()) {
            StructurePlacement placement = entry.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement concentricringsstructureplacement) {
                BlockPos blockpos = chunkGenerator.getNearestGeneratedStructure(pos, concentricringsstructureplacement);
                double d1 = pos.distSqr(blockpos);
                if (d1 < maxDist) {
                    maxDist = d1;
                    //TODO: readd
                    //foundStructures.add(Pair.of(blockpos, entry.getValue().iterator().next()));
                }
            } else if (placement instanceof RandomSpreadStructurePlacement randomPlacement) {
                list.add(Pair.of(randomPlacement, entry.getValue()));
                //finds the structure with the maximum spacing
                maxSpacing = Math.max(maxSpacing, randomPlacement.spacing());
            }
        }

        if (!list.isEmpty()) {
            int chunkX = SectionPos.blockToSectionCoord(pos.getX());
            int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
            long seed = level.getSeed();
            StructureFeatureManager manager = level.structureFeatureManager();

            //checks in increments based off maximum structure spacing on the list

            outerLoop:
            for (int k = 0; k <= maximumChunkDistance / maxSpacing; ++k) {

                int outerRing = (k + 1) * maxSpacing;
                int innerRing = k * maxSpacing;

                //less precision after 2k blocks
                boolean lessPrecision = innerRing * 16 > 2000;

                //<> madness
                //groups and orders all possible feature chunks ordered by RELATIVE ChunkPos. TreeMap is RB tree for fast additions
                TreeMap<Vec2i, List<Pair<RandomSpreadStructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>>>> possiblePositions = new TreeMap<>();

                for (Pair<RandomSpreadStructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> p : list) {
                    RandomSpreadStructurePlacement placement = p.getFirst();
                    int spacing = placement.spacing();

                    //checks all features in the area where the structure with the biggest spacing can spawn
                    for (int r = innerRing;  r < outerRing; r+=spacing) {
                        addAllPossibleFeatureChunksAtDistance(chunkX, chunkZ, r, seed, placement, c -> {
                            //converts chunkpos to relative pos, so they are ordered by distance already
                            var v = new Vec2i(c.x - chunkX, c.z - chunkZ);
                            if(possiblePositions.containsKey(v)){
                                int aaa = 1;
                            }
                            var ll = possiblePositions.computeIfAbsent(v, o -> new ArrayList<>());

                            if(ll.contains(p)){
                                //TODO: this should never be called... fix
                                int aaa = 1;
                            }
                            else ll.add(p);
                        });
                    }
                }

                //checks this first structure batch

                for (Vec2i vec2i : possiblePositions.keySet()) {
                    //check each chunkpos one by one
                    ChunkPos chunkPos = new ChunkPos(vec2i.x() + chunkX, vec2i.y() + chunkZ);
                    var structuresThatCanSpawnAtChunkPos = possiblePositions.get(vec2i);
                    for (var pp : structuresThatCanSpawnAtChunkPos) {
                        foundStructures.addAll(getFeaturesAtChunkPos(pp.getSecond(), level, manager, newlyGenerated, pp.getFirst(), chunkPos));
                    }
                    // after each it checks if criteria is met
                    if (foundStructures.size() >= requiredCount) {
                        break outerLoop;
                    }
                }
            }
        }
        //orders found by distance
        foundStructures.sort(Comparator.comparingDouble(f -> pos.distSqr(f.getFirst())));
        //returns only needed elements
        if (foundStructures.size() >= requiredCount) {
            return Lists.partition(foundStructures, requiredCount).get(0);
        }
        return foundStructures;
    }


    //gets all the chunk pos where a feature with a certain placement could spawn at a given radius from the center
    private static void addAllPossibleFeatureChunksAtDistance(int chunkX, int chunkZ, int radius, long seed, RandomSpreadStructurePlacement placement,
                                                              Consumer<ChunkPos> positionConsumer) {
        int spacing = placement.spacing();

        //checks square ring with radius
        for (int j = -radius; j <= radius; ++j) {
            boolean flag = j == -radius || j == radius;

            for (int k = -radius; k <= radius; ++k) {
                boolean flag1 = k == -radius || k == radius;
                if (flag || flag1) {
                    int px = chunkX + j;
                    int pz = chunkZ + k;
                    ChunkPos chunkpos = placement.getPotentialFeatureChunk(seed, px, pz);
                    positionConsumer.accept(chunkpos);
                }
            }
        }
    }

    //gets all features that are at a certain chunkpos with their position from a tag
    private static Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> getFeaturesAtChunkPos(
            Set<Holder<ConfiguredStructureFeature<?, ?>>> targets, LevelReader level, StructureFeatureManager featureManager,
            boolean newChunk, RandomSpreadStructurePlacement placement, ChunkPos chunkpos) {

        Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> foundStructures = new HashSet<>();
        //the target set usually contains 1 structure since it's very unlikely that 2 structures have the same placement
        for (Holder<ConfiguredStructureFeature<?, ?>> holder : targets) {
            //I believe this is what takes the most time to execute
            StructureCheckResult structurecheckresult = featureManager.checkStructurePresence(chunkpos, holder.value(), newChunk);
            if (structurecheckresult != StructureCheckResult.START_NOT_PRESENT) {
                if (!newChunk && structurecheckresult == StructureCheckResult.START_PRESENT) {
                    foundStructures.add(Pair.of(StructureFeature.getLocatePos(placement, chunkpos), holder));
                }

                ChunkAccess chunkaccess = level.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                StructureStart structurestart = featureManager.getStartForFeature(SectionPos.bottomOf(chunkaccess), holder.value(), chunkaccess);
                if (structurestart != null && structurestart.isValid()) {
                    if (newChunk && structurestart.canBeReferenced()) {
                        featureManager.addReference(structurestart);
                        foundStructures.add(Pair.of(StructureFeature.getLocatePos(placement, structurestart.getChunkPos()), holder));
                    }

                    if (!newChunk) {
                        foundStructures.add(Pair.of(StructureFeature.getLocatePos(placement, structurestart.getChunkPos()), holder));
                    }
                }
            }
        }
        return foundStructures;
    }

    @Nullable
    private static Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> getNearestGeneratedStructureAtDistance(
            Set<Holder<ConfiguredStructureFeature<?, ?>>> targets, LevelReader level, StructureFeatureManager featureManager,
            int x, int z, int distance, boolean newChunk, long seed, RandomSpreadStructurePlacement placement) {
        int i = placement.spacing();

        //checks square ring with radius=distance
        for (int j = -distance; j <= distance; ++j) {
            boolean flag = j == -distance || j == distance;

            for (int k = -distance; k <= distance; ++k) {
                boolean flag1 = k == -distance || k == distance;
                if (flag || flag1) {
                    int px = x + i * j;
                    int pz = z + i * k;
                    ChunkPos chunkpos = placement.getPotentialFeatureChunk(seed, px, pz);


                    return getFeaturesAtChunkPos(targets, level, featureManager, newChunk, placement, chunkpos);
                }
            }
        }

        return null;
    }


    //used for maps
    @Nullable
    public BlockPos findRandomMapFeature(TagKey<ConfiguredStructureFeature<?, ?>> tagKey, BlockPos pos,
                                         int radius, boolean unexplored, ServerLevel level) {
        if (!level.getServer().getWorldData().worldGenSettings().generateFeatures()) {
            return null;
        } else {
            Optional<HolderSet.Named<ConfiguredStructureFeature<?, ?>>> optional = level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).getTag(tagKey);
            if (optional.isEmpty()) {
                return null;
            } else {
                var o = optional.get();
                var list = o.stream().toList();
                var chosen = list.get(level.random.nextInt(list.size()));
                Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = level.getChunkSource().getGenerator().findNearestMapFeature(level,
                        HolderSet.direct(chosen), pos, radius, unexplored);
                return pair != null ? pair.getFirst() : null;
            }
        }
    }

}
