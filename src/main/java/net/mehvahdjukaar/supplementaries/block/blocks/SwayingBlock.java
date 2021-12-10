package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.SwayingBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public abstract class SwayingBlock extends WaterBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty EXTENSION = BlockProperties.EXTENSION;

    public SwayingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == stateIn.getValue(FACING).getOpposite() ? !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : getConnectedState(stateIn, facingState, worldIn, facingPos) : stateIn;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    public static BlockState getConnectedState(BlockState state, BlockState facingState, LevelAccessor world, BlockPos pos) {
        int ext = CommonUtil.getPostSize(facingState, pos, world);
        return state.setValue(EXTENSION, ext);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        super.entityInside(state, world, pos, entity);

        if (world.getBlockEntity(pos) instanceof SwayingBlockTile tile) {
            Vec3 mot = entity.getDeltaMovement();
            if (mot.length() > 0.05) {
                Vec3 norm = new Vec3(mot.x, 0, mot.z).normalize();
                Vec3i dv = tile.getDirection().getClockWise().getNormal();
                Vec3 vec = new Vec3(dv.getX(), 0, dv.getZ()).normalize();
                double dot = norm.dot(vec);
                if (dot != 0) {
                    tile.inv = dot < 0;
                }
                if (Math.abs(dot) > 0.4) tile.counter = 0;
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENSION, WATERLOGGED);
    }


}
