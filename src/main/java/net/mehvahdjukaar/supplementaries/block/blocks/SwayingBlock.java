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
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return facing == stateIn.get(FACING).getOpposite()?  !stateIn.isValidPosition(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState()
                : this.getConnectedState(stateIn,facingState, worldIn,facingPos) : stateIn;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    public BlockState getConnectedState(BlockState state, BlockState facingState, IWorld world, BlockPos pos){
        int ext = CommonUtil.getPostSize(facingState,pos,world);
        return state.with(EXTENSION, ext);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);

        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof SwayingBlockTile) {
            SwayingBlockTile te = ((SwayingBlockTile) tileentity);
            Vector3d mot = entity.getMotion();
            if(mot.length()>0.05){
                Vector3d norm = new Vector3d(mot.x,0,mot.z).normalize();
                Vector3i dv = te.getDirection().rotateY().getDirectionVec();
                Vector3d vec = new Vector3d(dv.getX(),0,dv.getZ()).normalize();
                double dot = norm.dotProduct(vec);
                if(dot!=0){
                    te.inv=dot<0;
                }
                if(Math.abs(dot)>0.4) te.counter = 0;
            }


        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENSION, WATERLOGGED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

}
