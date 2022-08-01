package net.mehvahdjukaar.supplementaries.common.block.tiles;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.common.world.generation.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.common.world.generation.StructureLocator;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//turn back now while you can. You have been warned
public class BlockGeneratorBlockTile extends BlockEntity {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private boolean firstTick = true;
    private volatile List<Pair<BlockPos, Holder<Structure>>> threadResult = null;

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
                    tile.threadResult = StructureLocator.findNearestMapFeatures(
                            world, ModTags.WAY_SIGN_DESTINATIONS, pos, 250,
                            false, 2);
                } catch (Exception e) {
                    tile.threadResult = null;
                }
            });
        }

        try {
            if (tile.threadResult != null) {
                RoadSignFeature.applyPostProcess((ServerLevel) level, pos, tile.threadResult);
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
