package net.mehvahdjukaar.supplementaries.world.structures;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;

public class StructureLocator {

    private static int dist(BlockPos pos1, BlockPos pos2) {
        int i = pos2.getX() - pos1.getX();
        int j = pos2.getZ() - pos1.getZ();
        return MathHelper.floor(MathHelper.sqrt((float)(i * i + j * j)));
    }


    public static Map<Integer, BlockPos> find(ServerWorld world, BlockPos pos, Structure<?> s, int range, int count){
        TreeMap<Integer,BlockPos> found = new TreeMap<>();

        //TODO: add to structure biome event

        if(world.getServer().getWorldData().worldGenSettings().generateFeatures() &&
            world.getChunkSource().generator.getBiomeSource().canGenerateStructure(s)){

            ChunkGenerator gen = world.getChunkSource().getGenerator();

            StructureSeparationSettings sepSettings = gen.getSettings().getConfig(s);

            if (sepSettings != null) {

                long seed = world.getSeed();

                StructureManager manager = world.structureFeatureManager();

                int spacing = sepSettings.spacing();
                int chunkX = pos.getX() >> 4;
                int chunkY = pos.getZ() >> 4;
                int r = 0;

                //checks in ever growing circles by increasing radius r
                for (SharedSeedRandom sharedseedrandom = new SharedSeedRandom(); r <= range; ++r) {
                    for (int x = -r; x <= r; ++x) {
                        boolean edgeX = x == -r || x == r;

                        for (int y = -r; y <= r; ++y) {
                            boolean edgeY = y == -r || y == r;
                            if (edgeX || edgeY) {
                                int k1 = chunkX + spacing * x;
                                int l1 = chunkY + spacing * y;
                                ChunkPos chunkpos = s.getPotentialFeatureChunk(sepSettings, seed, sharedseedrandom, k1, l1);
                                IChunk ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                                StructureStart<?> structureStart = manager.getStartForFeature(SectionPos.of(ichunk.getPos(), 0), s, ichunk);
                                if (structureStart != null && structureStart.isValid()) {
                                    BlockPos p = structureStart.getLocatePos();
                                    int distance = dist(pos,p);
                                    //discard one spawning in a village
                                    if(distance>64) found.put(distance,p);
                                    //checking all nearby villages to find the closest
                                }

                                if (r == 0) {
                                    break;
                                }
                                //less precision at long distances for performance
                                if (r>5 && found.size() >= count) break;
                            }
                        }

                        if (r == 0) {
                            break;
                        }
                        if (r>8 && found.size() >= count) break;

                    }
                    //exit loop
                    if (found.size() >= count) break;
                }

            }
        }
        return found;
    }



}
