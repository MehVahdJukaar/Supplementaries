package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WildFlaxBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<WildFlaxBlock> CODEC = simpleCodec(WildFlaxBlock::new);

    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public WildFlaxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return state.is(BlockTags.SAND);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        return rand.nextFloat() < 0.800000011920929D;
    }

    @Override
    public void performBonemeal(ServerLevel worldIn, RandomSource random, BlockPos pos, BlockState state) {
        int wildCropLimit = 10;

        for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (worldIn.getBlockState(blockpos).is(this)) {
                --wildCropLimit;
                if (wildCropLimit <= 0) {
                    return;
                }
            }
        }

        BlockPos blockPos = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

        for (int k = 0; k < 4; ++k) {
            if (worldIn.isEmptyBlock(blockPos) && state.canSurvive(worldIn, blockPos)) {
                pos = blockPos;
            }

            blockPos = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }

        if (worldIn.isEmptyBlock(blockPos) && state.canSurvive(worldIn, blockPos)) {
            worldIn.setBlock(blockPos, state, 2);
        }

    }
}
