package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModWorldgenRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.List;

public class CaveFilter extends PlacementFilter {

    public static final Codec<CaveFilter> CODEC =
            RecordCodecBuilder.create((instance) -> instance.group(
                            Heightmap.Types.CODEC.listOf().fieldOf("heightmaps").forGetter((p) -> p.belowHeightMaps),
                            Codec.BOOL.fieldOf("below_sea_level").forGetter(p -> p.belowSeaLevel)
                    )
                    .apply(instance, CaveFilter::new));

    public static class Type implements PlacementModifierType<CaveFilter> {
        @Override
        public Codec<CaveFilter> codec() {
            return CODEC;
        }
    }

    private final List<Heightmap.Types> belowHeightMaps;
    private final Boolean belowSeaLevel;

    private CaveFilter(List<Heightmap.Types> types, Boolean belowSeaLevel) {
        this.belowHeightMaps = types;
        this.belowSeaLevel = belowSeaLevel;
    }


    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        if (context.getLevel().getChunkSource() instanceof ServerChunkCache serverChunkCache) {
            int y = pos.getY();
            if(belowSeaLevel) {
                int sea = serverChunkCache.getGenerator().getSeaLevel();
                //below sea level
                if (y > sea) return false;
            }
            for (var h : belowHeightMaps) {
                int k = context.getHeight(h, pos.getX(), pos.getZ());
                if (y > k) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldgenRegistry.CAVE_MODIFIER.get();
    }


}