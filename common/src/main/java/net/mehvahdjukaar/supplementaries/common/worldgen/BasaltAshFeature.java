package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.mehvahdjukaar.supplementaries.common.worldgen.BasaltAshFeature.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Optional;

public class BasaltAshFeature extends Feature<Config> {

    public BasaltAshFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        int xzSpread = config.xzSpread + 1;
        int ySpread = config.ySpread;
        int tries = config.tries;
        RuleTest test = config.target;
        BlockStateProvider ash = config.ash;
        Optional<BlockState> belowAsh = config.belowAsh;
        RandomSource randomSource = context.random();
        BlockPos blockPos = context.origin();
        WorldGenLevel worldGenLevel = context.level();
        int placed = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        // Set<Pair<Integer, Integer>> blacklist = new HashSet<>();

        for (int l = 0; l < tries; ++l) {
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread), 0, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread));

            if (placeAsh(worldGenLevel, ySpread, mutableBlockPos, test, ash, belowAsh, randomSource)) {
                ++placed;
            }
        }

        return placed > 0;
    }

    public boolean placeAsh(WorldGenLevel worldGenLevel, int ySpread, BlockPos origin,
                            RuleTest basaltTest, BlockStateProvider ash, Optional<BlockState> belowAsh,
                            RandomSource random) {

        BlockPos.MutableBlockPos pos = origin.mutable();
        int inY = pos.getY();

        boolean success = false;
        int dy = 0;
        BlockState state = worldGenLevel.getBlockState(pos.setY(inY + dy++));
        boolean up = false;
        while (basaltTest.test(state, random) && dy < ySpread) {
            up = true;
            state = worldGenLevel.getBlockState(pos.setY(inY + dy++));
            if (state.isAir()) {
                success = true;
                dy -= 1;
                break;
            }
        }
        if (!up) {
            while (state.isAir() && dy > -ySpread) {
                state = worldGenLevel.getBlockState(pos.setY(inY + dy--));
                if (basaltTest.test(state, random)) {
                    success = true;
                    dy += 2;
                    break;
                }
            }
        }

        if (success) {

            pos.setY(inY + dy);
            worldGenLevel.setBlock(pos, ash.getState(random, pos), 2);
            pos.setY(inY + dy - 1);
            belowAsh.ifPresent(blockState -> worldGenLevel.setBlock(pos, blockState, 2));
        }

        return success;
    }

    public record Config(int tries, int xzSpread, int ySpread, RuleTest target,
                         BlockStateProvider ash, Optional<BlockState> belowAsh) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(Config::tries),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(Config::xzSpread),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(Config::ySpread),
                RuleTest.CODEC.fieldOf("target_predicate").forGetter(Config::target),
                BlockStateProvider.CODEC.fieldOf("top_block").forGetter(Config::ash),
                StrOpt.of(BlockState.CODEC, "below_block").forGetter(Config::belowAsh)
        ).apply(instance, Config::new));
    }
}
