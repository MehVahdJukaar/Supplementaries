package net.mehvahdjukaar.supplementaries.common.block.tiles;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.worldgen.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

//Turn back now while you can. You have been warned
public class BlockGeneratorBlockTile extends BlockEntity {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private final AtomicReference<List<StructureLocator.LocatedStruct>> threadResult = new AtomicReference<>(null);
    private boolean firstTick = true;
    private RoadSignFeature.Config config = null;

    public BlockGeneratorBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLOCK_GENERATOR_TILE.get(), pos, state);
    }

    //TODO: make them not spawn in villages

    public static void tick(Level level, BlockPos pos, BlockState state, BlockGeneratorBlockTile tile) {

        if (tile.firstTick) {
            tile.firstTick = false;

            ServerLevel serverLevel = (ServerLevel) level;
            EXECUTORS.submit(() -> {
                try {
                    tile.threadResult.set(StructureLocator.findNearestMapFeatures(
                            serverLevel, ModTags.WAY_SIGN_DESTINATIONS, pos, 250,
                            false, 2, false));
                } catch (Exception ignored) {
                }
            });
        }
        try {
            var result = tile.threadResult.get();
            if (result != null) {
                RoadSignFeature.applyPostProcess(tile.config, (ServerLevel) level, pos, result);
            }
        } catch (Exception e) {
            level.removeBlock(pos, false);
            Supplementaries.LOGGER.error("Failed to generate road sign at " + pos+": "+e);
        }
    }

    public void setConfig(RoadSignFeature.Config c) {
        this.config = c;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.config != null) {
            tag.put("config", RoadSignFeature.Config.CODEC.encodeStart(NbtOps.INSTANCE, this.config)
                    .getOrThrow(false, s -> {
                    }));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("config")) {
            this.config = RoadSignFeature.Config.CODEC.decode(NbtOps.INSTANCE, tag.get("config"))
                    .getOrThrow(true, s -> {
                    }).getFirst();
        }
    }
}
