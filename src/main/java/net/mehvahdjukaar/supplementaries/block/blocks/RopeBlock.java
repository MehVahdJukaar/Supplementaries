package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.Attachment;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Map;

public class RopeBlock extends Block implements IWaterLoggable{
    private static final VoxelShape SHAPE = Block.makeCuboidShape(1,1,1,15,15,15);
    public static final EnumProperty<Attachment> NORTH = BlockProperties.CONNECTION_NORTH;
    public static final EnumProperty<Attachment> SOUTH = BlockProperties.CONNECTION_SOUTH;
    public static final EnumProperty<Attachment> EAST = BlockProperties.CONNECTION_EAST;
    public static final EnumProperty<Attachment> WEST = BlockProperties.CONNECTION_WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE_0_7;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Map<Direction, EnumProperty<Attachment>> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
    });
    public RopeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(UP,true).with(DOWN,true).with(DISTANCE,7).with(WATERLOGGED,false)
                .with(NORTH,Attachment.NONE).with(SOUTH,Attachment.NONE).with(EAST,Attachment.NONE).with(WEST,Attachment.NONE));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH,SOUTH,EAST,WEST,UP,DOWN,WATERLOGGED,DISTANCE);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, net.minecraft.entity.LivingEntity entity) { return true; }


    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    public Attachment getAttachment(Block b){
        if(b == this)return Attachment.BLOCK;
        //else if(b instanceof FenceBlock)return Attachment.FENCE;
        //else if(b instanceof WallBlock)return Attachment.WALL;
        return Attachment.NONE;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        if(facing==Direction.UP) {
            boolean up = SackBlock.isSupportingCeiling(facingPos, worldIn);
            boolean down = up || worldIn.getBlockState(currentPos.down()).getBlock() == this;
            return stateIn.with(UP, up).with(DOWN, down);
        }
        else if(facing==Direction.DOWN){
            boolean down = facingState.getBlock() == this || stateIn.get(UP);
            return stateIn.with(DOWN,down);
        }
        else{
            return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing),this.getAttachment(facingState.getBlock()));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        boolean hasWater = context.getWorld().getFluidState(pos).getFluid() == Fluids.WATER;
        BlockState state = this.getDefaultState();
        for(Direction dir : FACING_TO_PROPERTY_MAP.keySet()){
            Block b = world.getBlockState(pos.offset(dir)).getBlock();
            state = state.with(FACING_TO_PROPERTY_MAP.get(dir),this.getAttachment(b));
        }
        boolean up = SackBlock.isSupportingCeiling(pos.up(), world);
        boolean down = up || world.getBlockState(pos.down()).getBlock() == this;
        state = state.with(UP,up).with(DOWN,down).with(WATERLOGGED,hasWater);
        return isValidPosition(state,world,pos)?state:null;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return!(!state.get(UP)&&state.get(NORTH).isNone()&&state.get(SOUTH).isNone()&&state.get(EAST).isNone()&&state.get(WEST).isNone());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(player.getHeldItem(handIn).getItem() == Registry.ROPE_ITEM.get()){
            if (this.addRope(pos.down(),worldIn)) return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }

    private boolean addRope(BlockPos pos, World world){
        Block block = world.getBlockState(pos).getBlock();
        if(this == block){
            return this.addRope(pos.down(),world);
        }
        else if(block.isReplaceable(this.getDefaultState(), Fluids.EMPTY)){
            world.setBlockState(pos,this.getDefaultState(),3);
            //TODO: add proper getDefault state and player on block placed stuff
            return true;
        }
        return false;
    }
}
