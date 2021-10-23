package net.mehvahdjukaar.supplementaries.world.structures;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class StructureLocator {

    private static final TreeMap<Float, Pair<Integer, Integer>> CHUNK_POSITIONS = new TreeMap<>();

    private static final List<StructureFeature<?>> TARGETS = new ArrayList<>();

    public static void init() {
        for (String name : ServerConfigs.spawn.SIGNS_VILLAGES.get()) {
            ResourceLocation res = new ResourceLocation(name);
            if (ForgeRegistries.STRUCTURE_FEATURES.containsKey(res))
                TARGETS.add(ForgeRegistries.STRUCTURE_FEATURES.getValue(res));
        }

        float range = 25;
        for (int r = 0; r <= range; ++r) {
            for (int x = -r; x <= r; ++x) {
                boolean edgeX = x == -r || x == r;
                for (int y = -r; y <= r; ++y) {
                    boolean edgeY = y == -r || y == r;
                    if (edgeX || edgeY) {
                        CHUNK_POSITIONS.put(Mth.sqrt(x * x + y * y), new ImmutablePair<>(x, y));
                    }
                }
            }
        }


    }

    private static int dist(BlockPos pos1, BlockPos pos2) {
        int i = pos2.getX() - pos1.getX();
        int j = pos2.getZ() - pos1.getZ();
        return (int) (Mth.sqrt((float) (i * i + j * j)));
    }

    private static float distance(BlockPos pos1, BlockPos pos2) {
        int i = pos2.getX() - pos1.getX();
        int j = pos2.getZ() - pos1.getZ();
        return Mth.sqrt((float) (i * i + j * j));
    }

    //doesn't work. not precise and slower in cases
    public static Pair<TreeMap<Float, BlockPos>, Boolean> findFast(ServerLevel world, BlockPos pos, int count) {
        TreeMap<Float, BlockPos> found = new TreeMap<>();
        //List<Pair<Integer,BlockPos>> found = new ArrayList<>();

        boolean inVillage = false;

        if (world.getServer().getWorldData().worldGenSettings().generateFeatures()) {

            ChunkGenerator gen = world.getChunkSource().getGenerator();
            BiomeSource biomeSource = gen.getBiomeSource();

            List<StructureFeature<?>> possibleTargets = new ArrayList<>();
            List<StructureFeatureConfiguration> sepSettings = new ArrayList<>();

            for (StructureFeature<?> str : TARGETS) {
                if (biomeSource.canGenerateStructure(str)) {
                    StructureFeatureConfiguration sep = gen.getSettings().getConfig(str);
                    if (sep != null) {
                        possibleTargets.add(str);
                        sepSettings.add(sep);
                    }
                }
            }

            long seed = world.getSeed();

            StructureFeatureManager manager = world.structureFeatureManager();

            int chunkX = pos.getX() >> 4;
            int chunkY = pos.getZ() >> 4;

            //checks in ever growing circles by increasing radius r
            WorldgenRandom sharedseedrandom = new WorldgenRandom();
            float lastDist = 0;
            for (float key : CHUNK_POSITIONS.keySet()) {
                Pair<Integer, Integer> pair = CHUNK_POSITIONS.get(key);
                int x = pair.getLeft();
                int y = pair.getRight();

                for (int ind = 0; ind < possibleTargets.size(); ind++) {

                    StructureFeatureConfiguration settings = sepSettings.get(ind);
                    StructureFeature<?> s = possibleTargets.get(ind);

                    int spacing = settings.spacing();

                    int k1 = chunkX + spacing * x;
                    int l1 = chunkY + spacing * y;
                    ChunkPos chunkpos = s.getPotentialFeatureChunk(settings, seed, sharedseedrandom, k1, l1);
                    ChunkAccess ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart<?> structureStart = manager.getStartForFeature(SectionPos.of(ichunk.getPos(), 0), s, ichunk);
                    if (structureStart != null && structureStart.isValid()) {
                        BlockPos p = structureStart.getLocatePos();
                        float distance = distance(pos, p);
                        //discard one spawning in a village
                        if (distance > 90) found.put(distance, p);
                        else inVillage = true;

                        if (found.size() == count){
                            lastDist = key;
                        }
                        //checking all nearby villages to find the closest
                    }
                }
                //exit loop
                if (found.size() >= count && key > lastDist + 0.5) break;
            }
        }

        //sort
        return new ImmutablePair<>(found, inVillage);
    }


    public static Pair<List<Pair<Integer, BlockPos>>, Boolean> find(ServerLevel world, int posX, int posZ, int count) {
        //TreeMap<Integer,BlockPos> found = new TreeMap<>();
        List<Pair<Integer, BlockPos>> found = new ArrayList<>();

        boolean inVillage = false;

        if (world.getServer().getWorldData().worldGenSettings().generateFeatures()) {

            ChunkGenerator gen = world.getChunkSource().getGenerator();
            BiomeSource biomeSource = gen.getBiomeSource();

            List<StructureFeature<?>> possibleTargets = new ArrayList<>();
            List<StructureFeatureConfiguration> sepSettings = new ArrayList<>();


            //TODO: cache some of this
            for (StructureFeature<?> str : TARGETS) {
                if (biomeSource.canGenerateStructure(str)) {
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
            int r = 0;

            int range = 25;

            //checks in ever growing circles by increasing radius r
            for (WorldgenRandom sharedseedrandom = new WorldgenRandom(); r <= range; ++r) {

                for (int ind = 0; ind < possibleTargets.size(); ind++) {

                    for (int x = -r; x <= r; ++x) {
                        boolean edgeX = x == -r || x == r;

                        for (int y = -r; y <= r; ++y) {
                            boolean edgeY = y == -r || y == r;
                            if (edgeX || edgeY) {

                                StructureFeatureConfiguration settings = sepSettings.get(ind);
                                StructureFeature<?> s = possibleTargets.get(ind);

                                int spacing = settings.spacing();

                                int k1 = chunkX + spacing * x;
                                int l1 = chunkY + spacing * y;
                                ChunkPos chunkpos = s.getPotentialFeatureChunk(settings, seed, sharedseedrandom, k1, l1);
                                ChunkAccess ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                                StructureStart<?> structureStart = manager.getStartForFeature(SectionPos.of(ichunk.getPos(), 0), s, ichunk);
                                if (structureStart != null && structureStart.isValid()) {
                                    BlockPos p = structureStart.getLocatePos();
                                    int distance = dist(new BlockPos(posX,0,posZ), p);
                                    //discard one spawning in a village
                                    if (distance > 90) found.add(new ImmutablePair<>(distance, p));
                                    else inVillage = true;
                                    //checking all nearby villages to find the closest
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


}
