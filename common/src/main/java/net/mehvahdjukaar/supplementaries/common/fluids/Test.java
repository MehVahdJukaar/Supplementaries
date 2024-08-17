package net.mehvahdjukaar.supplementaries.common.fluids;

import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LunchBoxBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Test extends FlammableLiquidBlock {
    public Test(Supplier<? extends FiniteFluid> supplier, Properties arg, int baseLight) {
        super(supplier, arg, baseLight);
    }

    @Override
    protected int getReactToFireDelay() {
        return 2;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) return;
        int age = state.getValue(AGE);
        FireStage stage = FireStage.fromAge(age);

        if (stage == FireStage.OFF) {
            // lights up from neighbors
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN) continue;
                if (GunpowderBlock.isFireSource(level, pos.relative(dir))) {
                    //plays sound too
                    this.lightUp(null, state, pos, level, FireSourceType.FIRE_CHANGE);
                    return;
                }
            }
            return;
        }

        // super.tick(state, level, pos, random);
        if (stage == FireStage.RISING) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            level.scheduleTick(pos, this, getReactToFireDelay());
            return;
        }

        if(true)return;
        if (stage == FireStage.RAGING) {
            level.scheduleTick(pos, this, getFireTickDelay(level.random));

            //tick normal fire

            int layers = state.getValue(MISSING_LEVELS);
            if (age == 15) {
                if (layers == 15) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    return;
                }
                level.setBlockAndUpdate(pos, state.setValue(MISSING_LEVELS, layers + 1));
                return;
            }

            int ageAdd = random.nextInt(3) / 2;
            int ageIncrease = Math.min(15, age + ageAdd);
            if (age != ageIncrease) {
                state = state.setValue(AGE, ageIncrease);
                level.setBlock(pos, state, 4);
            }

            //  if (age == 15 && random.nextInt(4) == 0 && !SuppPlatformStuff.canCatchFire(level, pos.below(), Direction.UP)) {
            //      level.removeBlock(pos, false);
            //      return;
            //  }

            boolean burnout = level.getBiome(pos).is(BiomeTags.INCREASED_FIRE_BURNOUT);
            int k = burnout ? -50 : 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        }


    }


    @Override
    public boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor level, FireSourceType fireSourceType) {
        setLitUp(state, level, pos, true);
        level.scheduleTick(pos, this, getReactToFireDelay());
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        //super.randomTick(state, level, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }
}
