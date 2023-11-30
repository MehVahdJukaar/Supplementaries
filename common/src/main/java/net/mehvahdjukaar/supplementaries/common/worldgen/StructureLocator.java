package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.Consumer;


public class StructureLocator {

    private static final Comparator<Vector2i> COMPARATOR = (o1, o2) -> Float.compare(o1.lengthSquared(), o2.lengthSquared());

    @Nullable
    public static LocatedStruct findNearestRandomMapFeature(
            ServerLevel level, @NotNull HolderSet<Structure> targets, BlockPos pos,
            int maximumChunkDistance, boolean newlyGenerated) {
        var found = findNearestMapFeatures(level, targets, pos, maximumChunkDistance,
                newlyGenerated, 1, false);
        if (!found.isEmpty()) return found.get(0);
        return null;
    }

    public static List<LocatedStruct> findNearestMapFeatures(
            ServerLevel level, @NotNull TagKey<Structure> tagKey, BlockPos pos,
            int maximumChunkDistance, boolean newlyGenerated, int requiredCount, boolean selectRandom) {

        var targets = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(tagKey).orElse(null);
        if (targets == null) return List.of();
        return findNearestMapFeatures(level, targets, pos, maximumChunkDistance, newlyGenerated, requiredCount, selectRandom);
    }


    public static List<LocatedStruct> findNearestMapFeatures(
            ServerLevel level, HolderSet<Structure> taggedStructures, BlockPos pos,
            int maximumChunkDistance, boolean newlyGenerated, int requiredCount, boolean selectRandom) {

        List<LocatedStruct> foundStructures = new ArrayList<>();

        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return foundStructures;
        }

        List<Holder<Structure>> selectedTargets = taggedStructures.stream().toList();

        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        double maxDist = Double.MAX_VALUE;

        //for adventure maps
        if (selectRandom) {
            Holder<Structure> selected = selectedTargets.get(level.random.nextInt(selectedTargets.size()));
            selectedTargets = List.of(selected);
        }

        //structures that can generate
        Map<StructurePlacement, Set<Holder<Structure>>> reachableTargetsMap = new Object2ObjectArrayMap<>();
        ChunkGeneratorStructureState structureState = level.getChunkSource().getGeneratorState();

        for (var holder : selectedTargets) {
            //if it can't generate in these biomes it won't return anything here so previous biome check isnt needed
            for (StructurePlacement structureplacement : structureState.getPlacementsForStructure(holder)) {
                reachableTargetsMap.computeIfAbsent(structureplacement, (placement) -> new ObjectArraySet<>()).add(holder);
            }
        }
        if (reachableTargetsMap.isEmpty()) return foundStructures;

        List<Pair<RandomSpreadStructurePlacement, Set<Holder<Structure>>>> list = new ArrayList<>(reachableTargetsMap.size());

        int maxSpacing = 1;

        StructureManager structuremanager = level.structureManager();

        //strongholds
        for (Map.Entry<StructurePlacement, Set<Holder<Structure>>> entry : reachableTargetsMap.entrySet()) {
            StructurePlacement placement = entry.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement concentricringsstructureplacement) {
                //calling this for concentric ring (stronghold) features. Other ones use custom method
                var foundPair = chunkGenerator.getNearestGeneratedStructure(entry.getValue(), level,
                        structuremanager, pos, newlyGenerated, concentricringsstructureplacement);
                if (foundPair != null) {
                    double d1 = pos.distSqr(foundPair.getFirst());
                    if (d1 < maxDist) {
                        maxDist = d1;
                        foundStructures.add(new LocatedStruct(foundPair));
                    }
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

            StructureManager manager = level.structureManager();

            //checks in increments based off maximum structure spacing on the list

            outerLoop:
            for (int k = 0; k <= maximumChunkDistance / maxSpacing; ++k) {

                int outerRing = (k + 1) * maxSpacing;
                int innerRing = k * maxSpacing;

                //less precision after 2k blocks
                boolean lessPrecision = innerRing * 16 > 2000;

                //<> madness
                //groups and orders all possible feature chunks ordered by RELATIVE ChunkPos. TreeMap is RB tree for fast additions
                TreeMap<Vector2i, List<Pair<RandomSpreadStructurePlacement, Set<Holder<Structure>>>>> possiblePositions = new TreeMap<>(COMPARATOR);

                for (Pair<RandomSpreadStructurePlacement, Set<Holder<Structure>>> p : list) {
                    RandomSpreadStructurePlacement placement = p.getFirst();
                    int spacing = placement.spacing();

                    //checks all features in the area where the structure with the biggest spacing can spawn
                    for (int r = innerRing; r < outerRing; r += spacing) {
                        addAllPossibleFeatureChunksAtDistance(chunkX, chunkZ, r, seed, placement, c -> {
                            //converts chunkpos to relative pos, so they are ordered by distance already
                            var v = new Vector2i(c.x - chunkX, c.z - chunkZ);
                            if (possiblePositions.containsKey(v)) {
                                int aaa = 1;
                            }
                            var ll = possiblePositions.computeIfAbsent(v,
                                    o -> new ArrayList<>());

                            if (ll.contains(p)) {
                                //TODO: this should never be called... fix
                                int aaa = 1;
                            } else ll.add(p);
                        });
                    }
                }

                //checks this first structure batch

                for (var e : possiblePositions.entrySet()) {
                    var vec2i = e.getKey();
                    //check each chunkpos one by one
                    ChunkPos chunkPos = new ChunkPos(vec2i.x() + chunkX, vec2i.y() + chunkZ);
                    var structuresThatCanSpawnAtChunkPos = e.getValue();
                    for (var pp : structuresThatCanSpawnAtChunkPos) {
                        foundStructures.addAll(getStructuresAtChunkPos(pp.getSecond(), level, manager, newlyGenerated, pp.getFirst(), chunkPos));
                    }
                    // after each it checks if criteria is met
                    if (foundStructures.size() >= requiredCount) {
                        break outerLoop;
                    }
                }
            }
        }
        //orders found by distance
        foundStructures.sort(Comparator.comparingDouble(f -> pos.distSqr(f.pos)));
        //returns only needed elements
        if (foundStructures.size() >= requiredCount) {
            return Lists.partition(foundStructures, requiredCount).get(0);
        }
        //add references to selected ones
        if (newlyGenerated) {
            for (var s : foundStructures) {
                if (s.start != null && s.start.canBeReferenced()) structuremanager.addReference(s.start);
            }
        }
        return foundStructures;
    }


    //gets all the chunk pos where a feature with a certain placement could spawn at a given radius from the center
    private static void addAllPossibleFeatureChunksAtDistance(int chunkX, int chunkZ, int radius, long seed, RandomSpreadStructurePlacement placement,
                                                              Consumer<ChunkPos> positionConsumer) {
        //int spacing = placement.spacing();

        //checks square ring with radius
        for (int j = -radius; j <= radius; ++j) {
            boolean flag = j == -radius || j == radius;

            for (int k = -radius; k <= radius; ++k) {
                boolean flag1 = k == -radius || k == radius;
                if (flag || flag1) {
                    int px = chunkX + j;
                    int pz = chunkZ + k;
                    ChunkPos chunkpos = placement.getPotentialStructureChunk(seed, px, pz);
                    positionConsumer.accept(chunkpos);
                }
            }
        }
    }

    //gets all features that are at a certain chunks with their position from a tag
    //like getStructuresGeneratingAt but gathers all possible structures at a chunk not just one
    private static Set<LocatedStruct> getStructuresAtChunkPos(
            Set<Holder<Structure>> targets, LevelReader level, StructureManager structureManager,
            boolean skipKnown, RandomSpreadStructurePlacement placement, ChunkPos chunkpos) {

        Set<LocatedStruct> foundStructures = new HashSet<>();
        //the target set usually contains 1 structure since it's very unlikely that 2 structures have the same placement

        for (Holder<Structure> holder : targets) {
            //I believe this is what takes the most time to execute
            StructureCheckResult structurecheckresult = structureManager.checkStructurePresence(chunkpos, holder.value(), skipKnown);
            if (structurecheckresult != StructureCheckResult.START_NOT_PRESENT) {
                if (!skipKnown && structurecheckresult == StructureCheckResult.START_PRESENT) {
                    //for not new chunk the ones without start are grabbed too?
                    foundStructures.add(new LocatedStruct(placement.getLocatePos(chunkpos), holder, null));
                } else {
                    ChunkAccess chunkaccess = level.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart structurestart = structureManager.getStartForStructure(SectionPos.bottomOf(chunkaccess), holder.value(), chunkaccess);
                    if (structurestart != null && structurestart.isValid() &&
                            (!skipKnown || structurestart.canBeReferenced())) { //if it has not other references
                        foundStructures.add(new LocatedStruct(
                                placement.getLocatePos(structurestart.getChunkPos()),
                                holder,
                                structurestart));
                    }
                }
            }
        }
        return foundStructures;
    }


    @Nullable
    private static Set<LocatedStruct> getNearestGeneratedStructureAtDistance(
            Set<Holder<Structure>> targets, LevelReader level, StructureManager featureManager,
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
                    ChunkPos chunkpos = placement.getPotentialStructureChunk(seed, px, pz);


                    return getStructuresAtChunkPos(targets, level, featureManager, newChunk, placement, chunkpos);
                }
            }
        }

        return null;
    }


    //used for maps
    @Nullable
    public BlockPos findRandomMapFeature(TagKey<Structure> tagKey, BlockPos pos,
                                         int radius, boolean unexplored, ServerLevel level) {
        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return null;
        } else {
            Optional<HolderSet.Named<Structure>> optional = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(tagKey);
            if (optional.isEmpty()) {
                return null;
            } else {

                var set = optional.get();
                var list = set.stream().toList();
                var chosen = list.get(level.random.nextInt(list.size()));
                Pair<BlockPos, Holder<Structure>> pair = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                        HolderSet.direct(chosen), pos, radius, unexplored);
                return pair != null ? pair.getFirst() : null;
            }
        }
    }

    public record LocatedStruct(BlockPos pos, Holder<Structure> structure, @Nullable StructureStart start) {
        public LocatedStruct(Pair<BlockPos, Holder<Structure>> pair) {
            this(pair.getFirst(), pair.getSecond(), null);
        }
    }

}
