package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.worldgen.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Turn back now while you can. You have been warned
public class BlockGeneratorBlockTile extends BlockEntity {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private CompletableFuture<List<StructureLocator.LocatedStruct>> threadResult;
    private boolean firstTick = true;
    private RoadSignFeature.Config config = null;

    public BlockGeneratorBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLOCK_GENERATOR_TILE.get(), pos, state);
    }

    //TODO: make them not spawn in villages

    public static void tick(Level level, BlockPos pos, BlockState state, BlockGeneratorBlockTile tile) {

        if (tile.firstTick) {
            tile.firstTick = false;

            tile.threadResult = CompletableFuture.supplyAsync(() -> StructureLocator.findNearestMapFeatures(
                    (ServerLevel) level, ModTags.ROAD_SIGN_DESTINATIONS, pos, 250,
                    false, 2, CommonConfigs.Building.ROAD_SIGN_MAX_SEARCHES.get(),
                            CommonConfigs.Building.ROAD_SIGN_EXIT_EARLY.get()),
                    EXECUTORS).exceptionally(exception -> {
                throwError(pos, exception);
                return null; // Handle exception by returning null
            });
            return;
        }
        if (tile.config == null || tile.threadResult == null || tile.threadResult.isCompletedExceptionally()) {
            level.removeBlock(pos, false);
            return;
        }

        if (tile.threadResult.isDone()) {
            try {
                RoadSignFeature.applyPostProcess(tile.config, (ServerLevel) level, pos, tile.threadResult.get());
            } catch (Exception e) {
                level.removeBlock(pos, false);
                throwError(pos, e);
            }
        }
    }

    private static void throwError(BlockPos pos, Throwable exception) {
        Supplementaries.LOGGER.error("Failed to generate road sign at {}: {}", pos, exception);
    }

    public void setConfig(RoadSignFeature.Config c) {
        this.config = c;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.config != null) {
            var ops = registries.createSerializationContext(NbtOps.INSTANCE);
            tag.put("config", RoadSignFeature.Config.CODEC.encodeStart(ops, this.config).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("config")) {
            var ops = registries.createSerializationContext(NbtOps.INSTANCE);
            this.config = RoadSignFeature.Config.CODEC.decode(ops, tag.get("config"))
                    .getOrThrow().getFirst();
        }
    }
}
