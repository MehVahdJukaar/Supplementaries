package net.mehvahdjukaar.supplementaries.common.world.generation.structure;

import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.utils.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StructureLocator {

    private static int dist(BlockPos pos1, BlockPos pos2) {
        int i = pos2.getX() - pos1.getX();
        int j = pos2.getZ() - pos1.getZ();
        return (int) (Mth.sqrt((float) (i * i + j * j)));
    }

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


}
