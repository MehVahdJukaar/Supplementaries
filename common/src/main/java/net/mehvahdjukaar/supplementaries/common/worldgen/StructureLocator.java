package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.mehvahdjukaar.moonlight.api.util.math.CircularGridUtils;
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
import net.mehvahdjukaar.supplementaries.Supplementaries;
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
import net.minecraft.world.level.chunk.status.ChunkStatus;
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


public class StructureLocator {

    private static final Comparator<Vector2i> DISTANCE_COMPARATOR =
            (vec1, vec2) -> Float.compare(vec1.lengthSquared(), vec2.lengthSquared());

    @Nullable
    public static LocatedStructure findNearestStructure(
            ServerLevel level, @NotNull HolderSet<Structure> targetStructures, BlockPos searchCenter,
            int maximumChunkSearchRadius, boolean findNewlyGeneratedOnly, int maxStructuresToConsider,
            boolean stopSearchWhenFound) {

        var foundStructures = findNearestStructures(level, targetStructures, searchCenter,
                maximumChunkSearchRadius, findNewlyGeneratedOnly, 1, maxStructuresToConsider,
                stopSearchWhenFound);

        if (!foundStructures.isEmpty()) return foundStructures.get(0);
        return null;
    }

    public static List<LocatedStructure> findNearestStructures(
            ServerLevel level, @NotNull TagKey<Structure> structureTag, BlockPos searchCenter,
            int maximumChunkSearchRadius, boolean findNewlyGeneratedOnly, int requiredStructureCount,
            int maxStructuresToConsider, boolean stopSearchWhenFound) {

        HolderSet<Structure> targetStructures = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .getTag(structureTag).orElse(null);

        return findNearestStructures(level, targetStructures, searchCenter, maximumChunkSearchRadius,
                findNewlyGeneratedOnly, requiredStructureCount, maxStructuresToConsider,
                stopSearchWhenFound);
    }


    private record StructureAndPlacement(Holder<Structure> structure, RandomSpreadStructurePlacement placement) {
    }

    public static List<LocatedStructure> findNearestStructures(
            ServerLevel level, HolderSet<Structure> targetStructureSet, BlockPos searchCenter,
            int maximumChunkSearchRadius, boolean findNewlyGeneratedOnly, int requiredStructureCount,
            int maxStructuresToConsider, boolean stopSearchWhenFound) {

        if (targetStructureSet == null) return List.of();

        // Limit the number of structures to consider for performance
        if (targetStructureSet.size() > maxStructuresToConsider) {
            var structureList = new ArrayList<>(targetStructureSet.stream().toList());
            Collections.shuffle(structureList);
            targetStructureSet = HolderSet.direct(structureList.subList(0, maxStructuresToConsider));
        }

        List<LocatedStructure> foundStructures = new ArrayList<>();

        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return foundStructures;
        }
        if (targetStructureSet.size() == 0) {
            Supplementaries.LOGGER.error("Found empty target structures for structure map. It's likely some mod broke some vanilla tag. Check your logs!");
            return foundStructures;
        }

        List<Holder<Structure>> selectedStructures = targetStructureSet.stream().toList();

        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        double closestDistanceSquared = Double.MAX_VALUE;

        // Shuffle to avoid bias
        selectedStructures = new ArrayList<>(selectedStructures);
        Collections.shuffle(selectedStructures);
        Supplementaries.LOGGER.info("Searching for closest structure among {} from position {}",
                Arrays.toString(selectedStructures.stream()
                        .map(e -> e.unwrapKey().get()).toArray()), searchCenter);

        // Group structures by their placement type
        Map<StructurePlacement, Set<Holder<Structure>>> structuresByPlacement = new Object2ObjectArrayMap<>();
        ChunkGeneratorStructureState structureState = level.getChunkSource().getGeneratorState();

        for (var structureHolder : selectedStructures) {
            // Structures that can generate in current dimension/biomes
            for (StructurePlacement placement : structureState.getPlacementsForStructure(structureHolder)) {
                structuresByPlacement.computeIfAbsent(placement,
                        p -> new ObjectArraySet<>()).add(structureHolder);
            }
        }
        if (structuresByPlacement.isEmpty()) return foundStructures;

        //map of spread to structure placement ino
        //lists of structures(and their placement data) with same spacing
        Int2ObjectSortedMap<List<StructureAndPlacement>> spreadToStructures = new Int2ObjectAVLTreeMap<>();

        StructureManager structureManager = level.structureManager();

        // Handle concentric ring structures (strongholds)
        for (Map.Entry<StructurePlacement, Set<Holder<Structure>>> entry : structuresByPlacement.entrySet()) {
            StructurePlacement placement = entry.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement ringsPlacement) {
                // Use built-in method for concentric ring structures
                var foundStructurePair = chunkGenerator.getNearestGeneratedStructure(
                        entry.getValue(), level, structureManager, searchCenter,
                        findNewlyGeneratedOnly, ringsPlacement);
                if (foundStructurePair != null) {
                    double distanceSquared = searchCenter.distSqr(foundStructurePair.getFirst());
                    if (distanceSquared < closestDistanceSquared) {
                        closestDistanceSquared = distanceSquared;
                        foundStructures.add(new LocatedStructure(foundStructurePair));
                    }
                }
            } else if (placement instanceof RandomSpreadStructurePlacement randomPlacement) {
                int spacing = randomPlacement.spacing();
                var list = spreadToStructures.computeIfAbsent(spacing, s -> new ArrayList<>());
                for (var struct : entry.getValue()) {
                    list.add(new StructureAndPlacement(struct, randomPlacement));
                }
            }
        }

        if (!spreadToStructures.isEmpty()) {
            int centerChunkX = SectionPos.blockToSectionCoord(searchCenter.getX());
            int centerChunkZ = SectionPos.blockToSectionCoord(searchCenter.getZ());
            long worldSeed = level.getSeed();

            StructureManager manager = level.structureManager();

            FairRingIterator ringIterator = new FairRingIterator(
                    new ArrayList<>(spreadToStructures.keySet()),
                    maximumChunkSearchRadius, true);
            int lastGridScale = Integer.MAX_VALUE;

            Multimap<Integer, LocatedStructure> foundStructureByDistance = MultimapBuilder.treeKeys()
                    .arrayListValues().build();
            List<Pair<Vec2i, StructureAndPlacement>> candidatePosThisIteration = new ArrayList<>();

            while (ringIterator.hasNext()) {
                var ring = ringIterator.next();
                int gridScale = ring.gridSize(); //spread
                if (gridScale < lastGridScale) { //new iteration over all the scales
                    //check all found structures
                    flushCandidates(candidatePosThisIteration, foundStructureByDistance);


                    for (var entry : candidatePosThisIteration) {
                        Vec2i relativePos = entry.getFirst();
                        RandomSpreadStructurePlacement placement = entry.getSecond().placement;
                        var structure = entry.getSecond().structure;

                        // Convert back to absolute chunk position
                        ChunkPos chunkPos = new ChunkPos(relativePos.x() + centerChunkX, relativePos.y() + centerChunkZ);
                        LocatedStructure located = getStructureThatWillSpawnAt(
                                structure, level, manager,
                                findNewlyGeneratedOnly, placement,
                                chunkPos);
                        // Check if we found enough structures
                        if (foundStructures.size() >= requiredStructureCount) {
                            break outerSearchLoop;
                        }
                    }

                }
                lastGridScale = gridScale;

                var placementsInGrid = spreadToStructures.get(gridScale);
                int radius = ring.radius();
                Iterator<Vec2i> cellsInRing = CircularGridUtils.iterateInRing(0, 0, radius, gridScale);
                while (cellsInRing.hasNext()) {

                    Vec2i cell = cellsInRing.next();
                    for (var placementInfo : placementsInGrid) {
                        RandomSpreadStructurePlacement placement = placementInfo.placement;
                        var structure = placementInfo.structure;

                        int potentialChunkX = centerChunkX + cell.x();
                        int potentialChunkZ = centerChunkZ + cell.y();
                        ChunkPos structureChunk = placement.getPotentialStructureChunk(
                                worldSeed, potentialChunkX, potentialChunkZ);

                        Vec2i structureRelativePos = new Vec2i(structureChunk.x - centerChunkX,
                                structureChunk.z - centerChunkZ);

                        candidatePosThisIteration.add(Pair.of(structureRelativePos,
                                new StructureAndPlacement(structure, placement)));
                    }
                }
            }

            // Search in expanding rings based on maximum structure spacing
            outerSearchLoop:
            for (int ringIndex = 0; ringIndex <= maximumChunkSearchRadius / maximumStructureSpacing; ++ringIndex) {

                int outerRingRadius = (ringIndex + 1) * maximumStructureSpacing;
                int innerRingRadius = ringIndex * maximumStructureSpacing;

                // Groups all possible structure chunk positions by relative coordinates, ordered by distance
                TreeMap<Vector2i, List<Pair<RandomSpreadStructurePlacement, Set<Holder<Structure>>>>> candidatePositions =
                        new TreeMap<>(DISTANCE_COMPARATOR);

                for (Pair<RandomSpreadStructurePlacement, Set<Holder<Structure>>> placementPair : randomSpreadStructures) {
                    RandomSpreadStructurePlacement placement = placementPair.getFirst();
                    int spacing = placement.spacing();

                    // Check all positions where this structure could spawn in the current ring
                    for (int radius = innerRingRadius; radius < outerRingRadius; radius += spacing) {


                        // Iterate over the square ring at given radius
                        for (int dx = -ringRadius; dx <= ringRadius; ++dx) {
                            boolean isEdgeX = dx == -ringRadius || dx == ringRadius;

                            for (int dz = -ringRadius; dz <= ringRadius; ++dz) {
                                boolean isEdgeZ = dz == -ringRadius || dz == ringRadius;
                                if (isEdgeX || isEdgeZ) {
                                    int potentialChunkX = centerChunkX + dx;
                                    int potentialChunkZ = centerChunkZ + dz;
                                    ChunkPos structureChunk = placement.getPotentialStructureChunk(
                                            worldSeed, potentialChunkX, potentialChunkZ);

                                    // Convert to relative position for distance ordering
                                    var relativePos = new Vector2i(structureChunk.x - centerChunkX, structureChunk.z - centerChunkZ);
                                    var placementList = candidatePositions.computeIfAbsent(relativePos,
                                            pos -> new ArrayList<>());

                                    if (placementList.contains(placementPair)) {
                                        Supplementaries.error();
                                        // TODO: Fix duplicate placement issue
                                    } else placementList.add(placementPair);
                                }
                            }
                        }
                    }
                }

                // Check candidate positions in current ring
                // Use less precise search after 2000 blocks
                boolean useLessPreciseSearch = innerRingRadius * 16 > 2000;
                int stopSearchCount = (stopSearchWhenFound || useLessPreciseSearch) ?
                        requiredStructureCount : Integer.MAX_VALUE;

                for (var entry : candidatePositions.entrySet()) {
                    var relativePos = entry.getKey();
                    // Convert back to absolute chunk position
                    ChunkPos chunkPos = new ChunkPos(relativePos.x() + centerChunkX, relativePos.y() + centerChunkZ);
                    var placementsForThisPosition = entry.getValue();
                    for (var placementPair : placementsForThisPosition) {
                        foundStructures.addAll(getStructuresAtChunkPosition(
                                placementPair.getSecond(), level, manager,
                                findNewlyGeneratedOnly, placementPair.getFirst(),
                                chunkPos, stopSearchCount));
                    }
                    // Check if we found enough structures
                    if (foundStructures.size() >= requiredStructureCount) {
                        break outerSearchLoop;
                    }
                }
            }
        }

        // Sort found structures by distance
        foundStructures.sort(Comparator.comparingDouble(f -> searchCenter.distSqr(f.position)));

        // Return only the required number of structures
        if (foundStructures.size() >= requiredStructureCount) {
            foundStructures = Lists.partition(foundStructures, requiredStructureCount).get(0);
        }

        // Add references for newly generated structures
        if (findNewlyGeneratedOnly) {
            for (var structure : foundStructures) {
                if (structure.start != null && structure.start.canBeReferenced()) {
                    structureManager.addReference(structure.start);
                }
            }
        }
        return foundStructures;
    }


    // Gets all structures of given types that are located at a specific chunk position
    @Nullable
    private static LocatedStructure getStructureThatWillSpawnAt(
            Holder<Structure> targetStructures, LevelReader level,
            StructureManager structureManager, boolean skipKnownStructures,
            RandomSpreadStructurePlacement placement, ChunkPos chunkPosition) {

        LocatedStructure foundStructures = null;
        // The target set usually contains 1 structure since it's unlikely that
        // 2 structures have the same placement

        // This check is performance-sensitive
        StructureCheckResult checkResult = structureManager.checkStructurePresence(
                chunkPosition, targetStructures.value(), placement, skipKnownStructures);

        if (checkResult != StructureCheckResult.START_NOT_PRESENT) {
            if (!skipKnownStructures && checkResult == StructureCheckResult.START_PRESENT) {
                // For already generated chunks, include structures without start data
                foundStructures = new LocatedStructure(
                        placement.getLocatePos(chunkPosition),
                        targetStructures, null);
            } else {
                ChunkAccess chunk = level.getChunk(
                        chunkPosition.x, chunkPosition.z, ChunkStatus.STRUCTURE_STARTS);
                StructureStart structureStart = structureManager.getStartForStructure(
                        SectionPos.bottomOf(chunk), targetStructures.value(), chunk);

                if (structureStart != null && structureStart.isValid() &&
                        (!skipKnownStructures || structureStart.canBeReferenced())) {
                    foundStructures = new LocatedStructure(
                            placement.getLocatePos(structureStart.getChunkPos()),
                            targetStructures,
                            structureStart);
                }
            }
        }
        return foundStructures;
    }


    @Nullable
    private static Set<LocatedStructure> findNearestGeneratedStructureAtDistance(
            Set<Holder<Structure>> targetStructures, LevelReader level,
            StructureManager structureManager, int centerChunkX, int centerChunkZ,
            int searchDistance, boolean findNewlyGeneratedOnly, long worldSeed,
            RandomSpreadStructurePlacement placement) {

        int structureSpacing = placement.spacing();

        // Search the square ring at given distance
        for (int dx = -searchDistance; dx <= searchDistance; ++dx) {
            boolean isEdgeX = dx == -searchDistance || dx == searchDistance;

            for (int dz = -searchDistance; dz <= searchDistance; ++dz) {
                boolean isEdgeZ = dz == -searchDistance || dz == searchDistance;
                if (isEdgeX || isEdgeZ) {
                    int potentialChunkX = centerChunkX + structureSpacing * dx;
                    int potentialChunkZ = centerChunkZ + structureSpacing * dz;
                    ChunkPos structureChunk = placement.getPotentialStructureChunk(
                            worldSeed, potentialChunkX, potentialChunkZ);

                    return getStructuresAtChunkPosition(targetStructures, level,
                            structureManager, findNewlyGeneratedOnly, placement,
                            structureChunk, Integer.MAX_VALUE);
                }
            }
        }

        return null;
    }


    // Used for map items to find a random structure
    @Nullable
    public BlockPos findRandomStructure(TagKey<Structure> structureTag, BlockPos searchCenter,
                                        int searchRadius, boolean findUnexploredOnly,
                                        ServerLevel level) {
        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return null;
        } else {
            Optional<HolderSet.Named<Structure>> structureSet = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE).getTag(structureTag);

            if (structureSet.isEmpty()) {
                return null;
            } else {
                var structures = structureSet.get();
                var structureList = structures.stream().toList();
                var chosenStructure = structureList.get(level.random.nextInt(structureList.size()));
                Pair<BlockPos, Holder<Structure>> foundPair = level.getChunkSource()
                        .getGenerator().findNearestMapStructure(level,
                                HolderSet.direct(chosenStructure), searchCenter, searchRadius,
                                findUnexploredOnly);
                return foundPair != null ? foundPair.getFirst() : null;
            }
        }
    }

    public record LocatedStructure(BlockPos position, Holder<Structure> structure,
                                   @Nullable StructureStart start) {
        public LocatedStructure(Pair<BlockPos, Holder<Structure>> structurePair) {
            this(structurePair.getFirst(), structurePair.getSecond(), null);
        }
    }
}