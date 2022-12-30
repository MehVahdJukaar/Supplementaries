package net.mehvahdjukaar.supplementaries.common.world.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.world.generation.BasaltAshFeature.Config;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BasaltAshFeature extends Feature<Config> {

    public BasaltAshFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        int xzSpread = context.config().xzSpread+ 1;
        int ySpread = context.config().ySpread;
        int tries = context.config().tries;
        RandomSource randomSource = context.random();
        BlockPos blockPos = context.origin();
        WorldGenLevel worldGenLevel = context.level();
        int placed = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        // Set<Pair<Integer, Integer>> blacklist = new HashSet<>();

        for (int l = 0; l < tries; ++l) {
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread), 0, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread));

            if (placeAsh(worldGenLevel, ySpread, mutableBlockPos)) {
                ++placed;
            }
        }

        return placed > 0;
    }

    public boolean placeAsh(WorldGenLevel worldGenLevel, int ySpread, BlockPos origin) {

        BlockPos.MutableBlockPos pos = origin.mutable();
        int inY = pos.getY();

        boolean success = false;
        int dy = 0;
        BlockState state = worldGenLevel.getBlockState(pos.setY(inY + dy++));

        while (state == Blocks.BASALT.defaultBlockState() && dy < ySpread) {
            state = worldGenLevel.getBlockState(pos.setY(inY + dy++));
            if (state.isAir()) {
                success = true;
                break;
            }
        }

        if (success) {
            pos.setY(inY + dy - 2);
            worldGenLevel.setBlock(pos, ModRegistry.ASHEN_BASALT.get().defaultBlockState(), 2);
            pos.setY(inY + dy - 1);
            worldGenLevel.setBlock(pos, ModRegistry.ASH_BLOCK.get().defaultBlockState(), 2);
        }
        return success;
    }

    public record Config(int tries, int xzSpread, int ySpread) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(Config::tries),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(Config::xzSpread),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(Config::ySpread)
        ).apply(instance, Config::new));
    }
}
