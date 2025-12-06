package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class BarnaclesMultifaceGrowthFeature extends Feature<BarnaclesMultifaceGrowthFeature.Config> {

    public BarnaclesMultifaceGrowthFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel worldGenLevel = context.level();
        BlockPos startPos = context.origin();
        RandomSource randomSource = context.random();
        Config config = context.config();
        BlockState startState = worldGenLevel.getBlockState(startPos);
        if(startState.is(config.placeBlock)) {
            //TODO: change
            config.placeBlock.getSpreader().spreadFromRandomFaceTowardRandomDirection(
                    startState, worldGenLevel, startPos, randomSource);
            return true;
        }
        if (!isAirOrWater(startState)) {
            return false;
        } else {
            Collection<Direction> allDirs = config.getShuffledDirections(randomSource);
            if (placeGrowthIfPossible(worldGenLevel, startPos, worldGenLevel.getBlockState(startPos), config, randomSource, allDirs)) {
                return true;
            } else {
                //we are in air or water but cant attach HERE, we try to find a wall
                BlockPos.MutableBlockPos mutableBlockPos = startPos.mutable();
                boolean placed = false;
                for (Direction direction : allDirs) {
                    mutableBlockPos.set(startPos);
                    List<Direction> list2 = config.getShuffledDirectionsExcept(randomSource, direction.getOpposite());

                    for (int i = 0; i < config.searchRange; ++i) {
                        mutableBlockPos.setWithOffset(startPos, direction);
                        BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
                        if (!isAirOrWater(blockState) && !blockState.is(config.placeBlock)) {
                            break;
                        }

                        if (placeGrowthIfPossible(worldGenLevel, mutableBlockPos, blockState, config, randomSource, list2)) {
                            placed = true;
                            return true;

                        }
                    }
                }

                return placed;
            }
        }
    }

    public static boolean placeGrowthIfPossible(WorldGenLevel level, BlockPos pos, BlockState state, Config config, RandomSource random, Collection<Direction> directions) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();
        Iterator<Direction> sides = directions.iterator();

        Direction direction;
        BlockState blockState;
        do {
            if (!sides.hasNext()) {
                return false;
            }

            direction = sides.next();
            blockState = level.getBlockState(mutableBlockPos.setWithOffset(pos, direction));
        } while (cantBePlacedOn(blockState, config));

        BlockState blockState2 = config.placeBlock.getStateForPlacement(state, level, pos, direction);
        if (blockState2 == null) {
            return false;
        } else {
            level.setBlock(pos, blockState2, 3);
            if (random.nextFloat() < config.chanceOfSpreading) {
                config.placeBlock.getSpreader().spreadFromFaceTowardRandomDirection(blockState2, level, pos,
                        direction, random, false);
            }
            return true;
        }
    }

    private static boolean cantBePlacedOn(BlockState state, Config config) {
        return state.is(config.cantBePlacedOn) || isAirOrWater(state);
    }

    private static boolean isAirOrWater(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }


    private static boolean isCliffFace(BlockPos pos, Level leve, Direction myFace) {
        if (myFace.getAxis().isVertical()) return false;
        BlockState onBlock = leve.getBlockState(pos.relative(myFace.getOpposite()));

        return false;
    }


    public record Config(MultifaceBlock placeBlock,
                         int searchRange,
                         PlacementEnvironment environment,
                         boolean requiresWater,
                         float chanceOfSpreading,
                         HolderSet<Block> cantBePlacedOn) implements FeatureConfiguration {


        public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").flatXmap(Config::validate, DataResult::success).orElse((MultifaceBlock) Blocks.GLOW_LICHEN).forGetter((c) -> c.placeBlock),
                Codec.intRange(1, 64).optionalFieldOf("search_range", 10).forGetter((c) -> c.searchRange),
                PlacementEnvironment.CODEC.optionalFieldOf("placement_environment", PlacementEnvironment.ANY).forGetter((c) -> c.environment),
                Codec.BOOL.optionalFieldOf("requires_water", false).forGetter((c) -> c.requiresWater),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance_of_spreading", 0.5f).forGetter((c) -> c.chanceOfSpreading),
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("cant_be_placed_on").forGetter((c) -> c.cantBePlacedOn)
        ).apply(instance, Config::new));

        private static DataResult<MultifaceBlock> validate(Block block) {
            if (block instanceof MultifaceBlock multifaceBlock) {
                return DataResult.success(multifaceBlock);
            } else {
                return DataResult.error(() -> "Growth block should be a multiface block");
            }
        }

        public List<Direction> getShuffledDirectionsExcept(RandomSource random, Direction direction) {
            return Util.toShuffledList(Stream.of(Direction.values()).filter((direction2) -> direction2 != direction), random);
        }

        public Collection<Direction> getShuffledDirections(RandomSource random) {
            return Direction.allShuffled(random);
        }
    }

    private enum PlacementEnvironment implements StringRepresentable {
        ANY,
        CLIFFS;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static final Codec<PlacementEnvironment> CODEC = StringRepresentable.fromEnum(PlacementEnvironment::values);
    }
}
