package net.mehvahdjukaar.supplementaries.reg.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class CaveFilter extends PlacementFilter {

    public static final Codec<CaveFilter> CODEC =
            RecordCodecBuilder.create((instance) -> instance.group(
                            Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((p) -> p.belowHeightMap))
                    .apply(instance, CaveFilter::new));

    public static PlacementModifierType<CaveFilter> TYPE;

    public static final CaveFilter BELOW_SURFACE = new CaveFilter(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);

    private final Heightmap.Types belowHeightMap;

    private CaveFilter(Heightmap.Types types) {
        this.belowHeightMap = types;
    }


    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        if (context.getLevel().getChunkSource() instanceof ServerChunkCache serverChunkCache) {
            int sea = serverChunkCache.getGenerator().getSeaLevel();
            //below sea level
            int y = pos.getY();
            if (y > sea) return false;
            int k = context.getHeight(this.belowHeightMap, pos.getX(), pos.getZ());
            return y < k;
        }
        return false;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }

    //loads so it registers
    public static void init() {
        TYPE = Registry.register(Registry.PLACEMENT_MODIFIERS, Supplementaries.res("cave"), () -> CODEC);
    }


}