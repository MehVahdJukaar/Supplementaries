package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.google.common.collect.Lists;
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

import java.util.*;


public class StructureLocator {

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
                        .map(Holder::getRegisteredName).toArray()), searchCenter);

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
                        foundStructures.add(new LocatedStructure(foundStructurePair.getFirst(),
                                foundStructurePair.getSecond(), null, distanceSquared));
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
            int lastIteration = 0;

            List<Pair<ChunkPos, StructureAndPlacement>> candidatePosThisIteration = new ArrayList<>();

            outer:
            while (ringIterator.hasNext()) {
                var ring = ringIterator.next();
                int gridScale = ring.gridSize(); //spread
                var placementsInGrid = spreadToStructures.get(gridScale);
                int radius = ring.radius();
                // Supplementaries.LOGGER.info("Searching in ring with radius {} (grid scale {})", radius, gridScale);

                // Check candidate positions in current ring
                // Use less precise search after 2000 blocks
                boolean useLessPreciseSearch = stopSearchWhenFound || (radius * 16 > 2000);
                //we only flush here so the results are most accurate and we dont miss stuff thats closer
                if (lastIteration != ring.commonIterationsIndex()) { //new iteration over all the scales
                    lastIteration = ring.commonIterationsIndex();
                    //check all found structures
                    flushCandidates(level, searchCenter, findNewlyGeneratedOnly,
                            candidatePosThisIteration, manager, foundStructures);
                    // Check if we found enough structures
                    if (foundStructures.size() >= requiredStructureCount) {
                        break;
                    }
                }


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

                        //  Supplementaries.LOGGER.info("potential structure {} at chunk {}",structure.getRegisteredName(), structureChunk);

                        candidatePosThisIteration.add(Pair.of(structureChunk,
                                new StructureAndPlacement(structure, placement)));
                    }

                    if (useLessPreciseSearch) {
                        flushCandidates(level, searchCenter, findNewlyGeneratedOnly,
                                candidatePosThisIteration, manager, foundStructures);
                        // Check if we found enough structures
                        if (foundStructures.size() >= requiredStructureCount) {
                            break outer;
                        }
                    }
                }
            }
            // Final flush
            flushCandidates(level, searchCenter, findNewlyGeneratedOnly,
                    candidatePosThisIteration, manager, foundStructures);
        }

        // Sort found structures by distance
        foundStructures.sort(Comparator.comparingDouble(LocatedStructure::distSqrt));

        // Return only the required number of structures
        if (foundStructures.size() >= requiredStructureCount) {
            foundStructures = Lists.partition(foundStructures, requiredStructureCount).getFirst();
        }

        // Add references for newly generated structures
        if (findNewlyGeneratedOnly) {
            for (var structure : foundStructures) {
                if (structure.start() != null && structure.start().canBeReferenced()) {
                    structureManager.addReference(structure.start());
                }
            }
        }
        //print structure and name

        Supplementaries.LOGGER.info("\n Structure locator found {} structures: \n{}", foundStructures.size(),
                String.join("\n", foundStructures.stream().map(LocatedStructure::toString).toArray(CharSequence[]::new)));

        return foundStructures;
    }

    private static void flushCandidates(ServerLevel level, BlockPos searchCenter, boolean findNewlyGeneratedOnly,
                                        List<Pair<ChunkPos, StructureAndPlacement>> candidatePosThisIteration,
                                        StructureManager manager, List<LocatedStructure> found) {
        for (var entry : candidatePosThisIteration) {
            ChunkPos chunkPos = entry.getFirst();
            RandomSpreadStructurePlacement placement = entry.getSecond().placement;
            var structure = entry.getSecond().structure;

            LocatedStructure located = getStructureThatWillSpawnAt(
                    structure, level, manager,
                    findNewlyGeneratedOnly, placement,
                    chunkPos,
                    searchCenter);
            if (located != null) {
                found.add(located);
            }
        }
        candidatePosThisIteration.clear();
    }


    // Gets all structures of given types that are located at a specific chunk position
    @Nullable
    private static LocatedStructure getStructureThatWillSpawnAt(
            Holder<Structure> targetStructures, LevelReader level,
            StructureManager structureManager, boolean skipKnownStructures,
            RandomSpreadStructurePlacement placement, ChunkPos chunkPosition,
            BlockPos searchCenter) {

        LocatedStructure foundStructures = null;
        // The target set usually contains 1 structure since it's unlikely that
        // 2 structures have the same placement

        // This check is performance-sensitive
        StructureCheckResult checkResult = structureManager.checkStructurePresence(
                chunkPosition, targetStructures.value(), placement, skipKnownStructures);

        if (checkResult != StructureCheckResult.START_NOT_PRESENT) {
            if (!skipKnownStructures && checkResult == StructureCheckResult.START_PRESENT) {
                // For already generated chunks, include structures without start data
                foundStructures = LocatedStructure.relativeTo(
                        placement.getLocatePos(chunkPosition),
                        targetStructures, null, searchCenter);
            } else {
                ChunkAccess chunk = level.getChunk(
                        chunkPosition.x, chunkPosition.z, ChunkStatus.STRUCTURE_STARTS);
                StructureStart structureStart = structureManager.getStartForStructure(
                        SectionPos.bottomOf(chunk), targetStructures.value(), chunk);

                if (structureStart != null && structureStart.isValid() &&
                        (!skipKnownStructures || structureStart.canBeReferenced())) {
                    foundStructures = LocatedStructure.relativeTo(
                            placement.getLocatePos(structureStart.getChunkPos()),
                            targetStructures,
                            structureStart, searchCenter);
                }
            }
        }
        if (foundStructures != null) {
            //Supplementaries.LOGGER.info("Found structure {} at {}, chunk {}",
            //      targetStructures.getRegisteredName(), foundStructures.position(), chunkPosition);
        }
        return foundStructures;
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

}