package net.mehvahdjukaar.supplementaries.common.world.generation.structure;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.mehvahdjukaar.supplementaries.common.utils.VectorUtils.Vec2i;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
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
/*
    public static Pair<List<Pair<Integer, BlockPos>>, Boolean> find(ServerLevel world, int posX, int posZ, int count) {
        //TreeMap<Integer,BlockPos> found = new TreeMap<>();
        List<Pair<Integer, BlockPos>> found = new ArrayList<>();

        boolean inVillage = false;

        if (world.getServer().getWorldData().worldGenSettings().generateFeatures()) {

            ChunkGenerator gen = world.getChunkSource().getGenerator();
            //BiomeSource biomeSource = gen.getBiomeSource();

            List<StructureFeature<?>> possibleTargets = new ArrayList<>();
            List<StructureFeatureConfiguration> sepSettings = new ArrayList<>();


            //TODO: cache some of this
            for (StructureFeature<?> str : ModTags.VILLAGES.getValues()) {
                if (true) { //biomeSource.canGenerateStructure(str)
                    StructureFeatureConfiguration sep = gen.getSettings().getConfig(str);
                    if (sep != null) {
                        possibleTargets.add(str);
                        sepSettings.add(sep);
                    }
                }
            }

            long seed = world.getSeed();

            StructureFeatureManager manager = world.structureFeatureManager();


            int chunkX = posX >> 4;
            int chunkY = posZ >> 4;

            int range = 25;

            //checks in ever growing circles by increasing radius r
            for (int r = 0; r <= range; ++r) {

                for (int ind = 0; ind < possibleTargets.size(); ind++) {

                    for (int x = -r; x <= r; ++x) {
                        boolean edgeX = x == -r || x == r;

                        for (int y = -r; y <= r; ++y) {
                            boolean edgeY = y == -r || y == r;
                            if (edgeX || edgeY) {

                                StructureFeatureConfiguration settings = sepSettings.get(ind);
                                StructureFeature<?> structure = possibleTargets.get(ind);

                                int spacing = settings.spacing();

                                int k1 = chunkX + spacing * x;
                                int l1 = chunkY + spacing * y;
                                ChunkPos chunkpos = structure.getPotentialFeatureChunk(settings, seed, k1, l1);

                                StructureCheckResult structurecheckresult = manager.checkStructurePresence(chunkpos, structure, false);
                                if (structurecheckresult != StructureCheckResult.START_NOT_PRESENT) {

                                //telepatic grunt optimization. only checks biomes that can spawn said structure. world.getChunk is very costly
                               // if(world.getNoiseBiome((chunkpos.x << 2) + 2, 60, (chunkpos.z << 2) + 2).getGenerationSettings().isValidStart(structure)) {

                                    ChunkAccess ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                                    StructureStart<?> structureStart = manager.getStartForFeature(SectionPos.bottomOf(ichunk), structure, ichunk);
                                    //StructureStart<?> structureStart = manager.getStartForFeature(SectionPos.of(ichunk.getPos(), 0), structure, ichunk);
                                    if (structureStart != null && structureStart.isValid()) {
                                        BlockPos p = structure.getLocatePos(structureStart.getChunkPos());
                                        int distance = dist(new BlockPos(posX, 0, posZ), p);
                                        //discard one spawning in a village
                                        if (distance > 90) found.add(new ImmutablePair<>(distance, p));
                                        else inVillage = true;
                                        //checking all nearby villages to find the closest
                                    }
                                }

                                if (r == 0) {
                                    break;
                                }
                                //less precision at long distances for performance
                                if (r > 5 && found.size() >= count) break;
                            }
                        }

                        if (r == 0) {
                            break;
                        }
                        if (r > 8 && found.size() >= count) break;

                    }
                }
                //exit loop
                if (found.size() >= count) break;
            }
        }

        //sort

        Collections.sort(found);
        return new ImmutablePair<>(found, inVillage);
    }
*/

    public Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> findNearestMapFeature(
            ServerLevel level, HolderSet<ConfiguredStructureFeature<?, ?>> targets, BlockPos pos, int radius, boolean newChunks, int required) {

        Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> foundStructures = new HashSet<>();

        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        Set<Holder<Biome>> targetBiomes = targets.stream().flatMap((holder) -> holder.value().biomes().stream()).collect(Collectors.toSet());

        if (!targetBiomes.isEmpty()) {
            Set<Holder<Biome>> possibleBiomes = chunkGenerator.getBiomeSource().possibleBiomes();
            //if they have biomes in common
            if (!Collections.disjoint(possibleBiomes, targetBiomes)) {

                double maxDist = Double.MAX_VALUE;
                //structures that can generate
                Map<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> reachableTargets = new Object2ObjectArrayMap<>();

                for (Holder<ConfiguredStructureFeature<?, ?>> holder : targets) {
                    if (possibleBiomes.stream().anyMatch(holder.value().biomes()::contains)) {
                        for (StructurePlacement structureplacement : chunkGenerator.getPlacementsForFeature(holder)) {
                            reachableTargets.computeIfAbsent(structureplacement, (placement) -> new ObjectArraySet<>()).add(holder);
                        }
                    }
                }

                List<Pair<RandomSpreadStructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>>> list = new ArrayList<>(reachableTargets.size());

                int maxSpacing = 0;

                //stronghold hax
                for (Map.Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> entry : reachableTargets.entrySet()) {
                    StructurePlacement placement = entry.getKey();
                    if (placement instanceof ConcentricRingsStructurePlacement concentricringsstructureplacement) {
                        BlockPos blockpos = chunkGenerator.getNearestGeneratedStructure(pos, concentricringsstructureplacement);
                        double d1 = pos.distSqr(blockpos);
                        if (d1 < maxDist) {
                            maxDist = d1;
                            foundStructures.add(Pair.of(blockpos, entry.getValue().iterator().next()));
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

                    //change
                    for (int k = 0; k <= radius; ++k) {

                        //<> madness
                        //groups and orders all possible feature chunks ordered by RELATIVE ChunkPos. TreeMap is RB tree for fast additions
                        TreeMap<Vec2i, List<Pair<RandomSpreadStructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>>>> possiblePositions = new TreeMap<>();

                        for (Pair<RandomSpreadStructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> p : list) {
                            RandomSpreadStructurePlacement placement = p.getFirst();
                            int spacing = placement.spacing();

                            //checks all features in the area where the structure with the biggest spacing can spawn
                            for(int r = 0; spacing*r<maxSpacing; r++){
                                addAllPossibleFeatureChunksAt(chunkX, chunkZ,r, seed, placement, v->{
                                    var ll = possiblePositions.computeIfAbsent(v,o->new ArrayList<>());
                                    ll.add(p);
                                });
                            }
                            //checks this first structure batch

                            for(Vec2i vec2i : possiblePositions.keySet()){

                            }


                            Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair1 = getNearestGeneratedStructureAtDistance(entiy.getValue(), level,
                                    level.structureFeatureManager(), chunkX, chunkZ, k, newChunks, level.getSeed(), randomspreadstructureplacement);
                            if (pair1 != null) {
                                double d2 = pos.distSqr(pair1.getFirst());
                                if (d2 < maxDist) {
                                    maxDist = d2;
                                    foundStructures = pair1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return foundStructures;
    }


    private static void addAllPossibleFeatureChunksAt(int chunkX, int chunkZ, int radius, long seed, RandomSpreadStructurePlacement placement,
                                                            Consumer<Vec2i> positionConsumer) {
        int spacing = placement.spacing();

        //checks square ring with radius
        for (int j = -radius; j <= radius; ++j) {
            boolean flag = j == -radius || j == radius;

            for (int k = -radius; k <= radius; ++k) {
                boolean flag1 = k == -radius || k == radius;
                if (flag || flag1) {
                    int px = chunkX + spacing * j;
                    int pz = chunkZ + spacing * k;
                    ChunkPos chunkpos = placement.getPotentialFeatureChunk(seed, px, pz);
                    positionConsumer.accept(new Vec2i(chunkpos.x-chunkX, chunkpos.z-chunkZ));
                }
            }
        }
    }

    //gets all features that are at a certain chunkpos with their position from a tag
    private static Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> getFeaturesAtChunkPos(
            Set<Holder<ConfiguredStructureFeature<?, ?>>> targets, LevelReader level, StructureFeatureManager featureManager,
            boolean newChunk, RandomSpreadStructurePlacement placement, ChunkPos chunkpos) {

        Set<Pair<BlockPos, Holder<ConfiguredStructureFeature<?,?>>>> foundStructures = new HashSet<>();
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
                        foundStructures.add(Pair.of(StructureFeature.getLocatePos(placement, structurestart.getChunkPos()), holder);
                    }

                    if (!newChunk) {
                        foundStructures.add(Pair.of(StructureFeature.getLocatePos(placement, structurestart.getChunkPos()), holder);
                    }
                }
            }
        }
        return foundStructures;
    }

    @Nullable
    private static Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> getNearestGeneratedStructureAtDistance(
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


                    Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> placement1 = getFeaturesAtChunkPos(targets, level, featureManager, newChunk, placement, chunkpos);
                    if (placement1 != null) return placement1;
                }
            }
        }

        return null;
    }

}
