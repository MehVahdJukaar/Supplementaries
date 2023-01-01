package net.mehvahdjukaar.supplementaries.common.block.tiles;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.worldgen.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

//turn back now while you can. You have been warned
public class BlockGeneratorBlockTile extends BlockEntity {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private final AtomicReference<List<Pair<BlockPos, Holder<Structure>>>> threadResult = new AtomicReference<>(null);
    private boolean firstTick = true;

    public BlockGeneratorBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLOCK_GENERATOR_TILE.get(), pos, state);
    }

    //TODO: make them not spawn in villages

    public static void tick(Level level, BlockPos pos, BlockState state, BlockGeneratorBlockTile tile) {

        if (tile.firstTick) {
            tile.firstTick = false;

            ServerLevel world = (ServerLevel) level;

            EXECUTORS.submit(() -> {
                try {
                    tile.threadResult.set( StructureLocator.findNearestMapFeatures(
                            world, ModTags.WAY_SIGN_DESTINATIONS, pos, 250,
                            false, 2));
                } catch (Exception ignored) {
                }
            });
        }

        try {
           var result =  tile.threadResult.get();
            if (result != null) {
                RoadSignFeature.applyPostProcess((ServerLevel) level, pos, result);
            }
        } catch (Exception exception) {
            tile.failAndRemove(level, pos, exception);
        }
    }

    private void failAndRemove(Level level, BlockPos pos, Exception e) {
        level.removeBlock(pos, false);
        Supplementaries.LOGGER.warn("failed to generate road sign at " + pos + ": " + e);
    }



}
