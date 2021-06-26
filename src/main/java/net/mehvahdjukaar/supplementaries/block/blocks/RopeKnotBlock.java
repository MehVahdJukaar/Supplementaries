package net.mehvahdjukaar.supplementaries.block.blocks;


import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.block.tiles.RopeKnotBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RopeKnotBlock extends MimicBlock implements IWaterLoggable{

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<PostType> POST_TYPE = BlockProperties.POST_TYPE;

    protected static final Map<Direction, BooleanProperty> FENCE_PROPERTY = SixWayBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((d) -> d.getKey().getAxis().isHorizontal()).collect(Util.toMap());
    protected static final Map<Direction, EnumProperty<WallHeight>> WALL_PROPERTY = ImmutableMap.of(Direction.NORTH,WallBlock.NORTH_WALL,Direction.SOUTH,WallBlock.SOUTH_WALL,Direction.WEST,WallBlock.WEST_WALL,Direction.EAST,WallBlock.EAST_WALL);

    public RopeKnotBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y)
                .setValue(WATERLOGGED, false).setValue(POST_TYPE, PostType.POST));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        TileEntity te = world.getBlockEntity(currentPos);
        if(te instanceof RopeKnotBlockTile){
            IBlockHolder tile = ((IBlockHolder) te);
            BlockState oldHeld = tile.getHeldBlock();
            BlockState newHeld = oldHeld.updateShape(facing,facingState,world,currentPos,facingPos);

            //world.setBlock(currentPos,newHeld,2);
            BlockState newFacing = facingState.updateShape(facing.getOpposite(),newHeld,world,facingPos,currentPos);

            if(newFacing!=facingState){
                world.setBlock(facingPos,newFacing,2);
            }

            //BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            // world.setBlockAndUpdate(toPos, newState);

            if(newHeld!=oldHeld){
                tile.setHeldBlock(newHeld);
                te.setChanged();
            }
        }
        return state;
    }


    @Override
    public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
        switch(p_185499_2_) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch(p_185499_1_.getValue(AXIS)) {
                    case X:
                        return p_185499_1_.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return p_185499_1_.setValue(AXIS, Direction.Axis.X);
                    default:
                        return p_185499_1_;
                }
            default:
                return p_185499_1_;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }


    protected static final VoxelShape SHAPE_Y = Block.box(4D, 0D, 4.0D, 12.0D, 16D, 12.0D);

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof RopeKnotBlockTile){
            return ((IBlockHolder) te).getHeldBlock().getShape(world, pos, context);
        }
        return SHAPE_Y;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof RopeKnotBlockTile){
            return ((IBlockHolder) te).getHeldBlock().getShape(world, pos, context);
        }
        return SHAPE_Y;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED,POST_TYPE,AXIS);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RopeKnotBlockTile();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof RopeKnotBlockTile){
            return new ItemStack(((IBlockHolder) te).getHeldBlock().getBlock().asItem());
        }
        return super.getPickBlock(state,target,world,pos,player);
    }

    @Override
    public ActionResultType use(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult p_225533_6_) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof RopeKnotBlockTile){
            ItemStack stack = playerEntity.getItemInHand(hand);
            if(stack.getItem() instanceof BlockItem)
            ((IBlockHolder) te).setHeldBlock(((BlockItem) stack.getItem()).getBlock().defaultBlockState());
            te.setChanged();
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }

    public static boolean convertBlock(BlockState state, World world, BlockPos pos){

        PostType type = PostType.POST;
        boolean flag = false;
        if(state.is(ModTags.BEAMS)) {
            type = PostType.BEAM;
            flag = true;
        }
        else if(state.is(ModTags.PALISADES)) {
            type = PostType.PALISADE;
            flag = true;
        }
        else if(state.is(ModTags.POSTS)) {
            type = PostType.POST;
            flag = true;
        }
        else if(state.is(BlockTags.WALLS)) {
            if(state.hasProperty(WallBlock.UP) && state.getValue(WallBlock.UP)) {
                type = PostType.WALL;
            }
            else{
                type = PostType.PALISADE;
            }
            flag = true;
        }

        if(flag){

            Direction.Axis axis = Direction.Axis.Y;
            if(state.hasProperty(BlockStateProperties.AXIS)){
                axis = state.getValue(BlockStateProperties.AXIS);
            }
            BlockState newState = Registry.ROPE_KNOT.get().defaultBlockState()
                    .setValue(AXIS,axis).setValue(POST_TYPE,type);

            world.setBlockAndUpdate(pos,newState);
            TileEntity te = world.getBlockEntity(pos);
            if(te instanceof RopeKnotBlockTile){
                ((IBlockHolder) te).setHeldBlock(state);
                te.setChanged();
            }
            return true;
        }
        else return false;
    }

}
