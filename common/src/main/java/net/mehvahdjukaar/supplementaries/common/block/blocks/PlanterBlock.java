package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

public class PlanterBlock extends WaterBlock {

    protected static final VoxelShape SHAPE = Shapes.or(Shapes.box(0.125D, 0D, 0.125D, 0.875D, 0.687D, 0.875D), Shapes.box(0D, 0.687D, 0D, 1D, 1D, 1D));
    protected static final VoxelShape SHAPE_C = Shapes.or(Shapes.box(0, 0, 0, 1, 0.9375, 1));

    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

    public PlanterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENDED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE_C;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(EXTENDED, this.canConnect(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (facing == Direction.UP) {
            return stateIn.setValue(EXTENDED, this.canConnect(worldIn, currentPos));
        }
        return stateIn;
    }

    private boolean canConnect(LevelAccessor world, BlockPos pos) {
        BlockPos up = pos.above();
        BlockState state = world.getBlockState(up);
        Block b = state.getBlock();
        VoxelShape shape = state.getShape(world, up);
        boolean connect = (!shape.isEmpty() && shape.bounds().minY < 0.06);
        return (connect && !(b instanceof StemBlock) && !(b instanceof CropBlock));
    }

    //override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean onTreeGrow(BlockState state, LevelReader level, BiConsumer<BlockPos, BlockState> placeFunction, RandomSource randomSource, BlockPos pos, TreeConfiguration config) {
        if (CommonConfigs.Building.PLANTER_BREAKS.get()) {
            placeFunction.accept(pos, Blocks.ROOTED_DIRT.defaultBlockState());

            if (level instanceof Level l) {
                l.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.below(), net.minecraft.world.level.block.Block.getId(state));
                l.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 0.71f);
            }
            return true;
        }
        return false;
    }


}