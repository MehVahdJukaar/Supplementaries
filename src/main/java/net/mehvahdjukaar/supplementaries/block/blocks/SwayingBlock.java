package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.SwayingBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class SwayingBlock extends Block implements IWaterLoggable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty EXTENSION = BlockProperties.EXTENSION;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public SwayingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == stateIn.getValue(FACING).getOpposite()?  !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : getConnectedState(stateIn,facingState, worldIn,facingPos) : stateIn;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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

    public static BlockState getConnectedState(BlockState state, BlockState facingState, IWorld world, BlockPos pos){
        int ext = CommonUtil.getPostSize(facingState,pos,world);
        return state.setValue(EXTENSION, ext);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        super.entityInside(state, world, pos, entity);

        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SwayingBlockTile) {
            SwayingBlockTile te = ((SwayingBlockTile) tileentity);
            Vector3d mot = entity.getDeltaMovement();
            if(mot.length()>0.05){
                Vector3d norm = new Vector3d(mot.x,0,mot.z).normalize();
                Vector3i dv = te.getDirection().getClockWise().getNormal();
                Vector3d vec = new Vector3d(dv.getX(),0,dv.getZ()).normalize();
                double dot = norm.dot(vec);
                if(dot!=0){
                    te.inv=dot<0;
                }
                if(Math.abs(dot)>0.4) te.counter = 0;
            }


        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENSION, WATERLOGGED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

}
