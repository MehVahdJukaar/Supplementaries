package net.mehvahdjukaar.supplementaries.block.blocks;


import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.block.tiles.RopeKnotBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.DecoBlocksCompatRegistry;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RopeKnotBlock extends MimicBlock implements IWaterLoggable {

    private final Map<BlockState, VoxelShape> SHAPES_MAP = new HashMap<>();
    private final Map<BlockState, VoxelShape> COLLISION_SHAPES_MAP = new HashMap<>();

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<PostType> POST_TYPE = BlockProperties.POST_TYPE;

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;


    protected static final Map<Direction, BooleanProperty> FENCE_PROPERTY = SixWayBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((d) -> d.getKey().getAxis().isHorizontal()).collect(Util.toMap());
    protected static final Map<Direction, EnumProperty<WallHeight>> WALL_PROPERTY = ImmutableMap.of(Direction.NORTH, WallBlock.NORTH_WALL, Direction.SOUTH, WallBlock.SOUTH_WALL, Direction.WEST, WallBlock.WEST_WALL, Direction.EAST, WallBlock.EAST_WALL);

    public RopeKnotBlock(Properties properties) {
        super(properties);
        this.makeShapes();

        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y)
                .setValue(WATERLOGGED, false).setValue(POST_TYPE, PostType.POST)
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(EAST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, POST_TYPE, AXIS, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RopeKnotBlockTile();
    }



    /*
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COLLISION_SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }*/



    //this is madness

    /*
    @Override
    public VoxelShape getVisualShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }*/


    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof RopeKnotBlockTile) {
            try {
                return ((RopeKnotBlockTile) te).getShape();
            }catch (Exception ignored){}
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }


    @Override
    public VoxelShape getBlockSupportShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof RopeKnotBlockTile) {
            try {
                return ((RopeKnotBlockTile) te).getCollisionShape();
            }catch (Exception ignored){}
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    protected void makeShapes() {
        VoxelShape down = Block.box(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.box(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.box(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.box(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.box(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.box(6, 9, 6, 16, 13, 10);
        //VoxelShape knot = Block.box(6, 9, 6, 10, 13, 10);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.getValue(WATERLOGGED)) continue;

            VoxelShape v;
            VoxelShape c;
            int w = state.getValue(POST_TYPE).getWidth();
            int o = (16 - w) / 2;
            switch (state.getValue(AXIS)) {
                default:
                case Y:
                    v = Block.box(o, 0D, o, o + w, 16D, o + w);
                    c = Block.box(o, 0D, o, o + w, 24, o + w);
                    break;
                case X:
                    v = Block.box(0D, o, o, 16D, o + w, o + w);
                    c = v;
                    break;
                case Z:
                    v = Block.box(o, o, 0, o + w, o + w, 16);
                    c = v;
                    break;
            }
            if (state.getValue(DOWN)) v = VoxelShapes.or(v, down);
            if (state.getValue(UP)) v = VoxelShapes.or(v, up);
            if (state.getValue(NORTH)) v = VoxelShapes.or(v, north);
            if (state.getValue(SOUTH)) v = VoxelShapes.or(v, south);
            if (state.getValue(WEST)) v = VoxelShapes.or(v, west);
            if (state.getValue(EAST)) v = VoxelShapes.or(v, east);
            c = VoxelShapes.or(c, v);
            c = c.optimize();
            v = v.optimize();
            boolean flag = true;
            for (VoxelShape existing : this.SHAPES_MAP.values()) {
                if (existing.equals(v)) {
                    this.SHAPES_MAP.put(state, existing);
                    flag = false;
                    break;
                }
            }
            if (flag) this.SHAPES_MAP.put(state, v);

            boolean flag2 = true;
            for (VoxelShape existing : this.COLLISION_SHAPES_MAP.values()) {
                if (existing.equals(c)) {
                    this.COLLISION_SHAPES_MAP.put(state, existing);
                    flag2 = false;
                    break;
                }
            }
            if (flag2) this.COLLISION_SHAPES_MAP.put(state, c);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        BlockState newState = state.setValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(facing), RopeBlock.shouldConnectToFace(state, facingState, facingPos, facing, world));
        TileEntity te = world.getBlockEntity(currentPos);
        if (te instanceof RopeKnotBlockTile) {
            IBlockHolder tile = ((IBlockHolder) te);
            BlockState oldHeld = tile.getHeldBlock();

            RopeKnotBlockTile otherTile = null;
            if (facingState.getBlock().is(ModRegistry.ROPE_KNOT.get())) {
                TileEntity te2 = world.getBlockEntity(facingPos);
                if (te2 instanceof RopeKnotBlockTile) {
                    otherTile = ((RopeKnotBlockTile) te2);
                    facingState = otherTile.getHeldBlock();
                }
            }

            BlockState newHeld = null;

            if (CompatHandler.quark) {
                newHeld = QuarkPlugin.updateWoodPostShape(oldHeld, facing, facingState);
            }
            if (newHeld == null) {
                newHeld = oldHeld.updateShape(facing, facingState, world, currentPos, facingPos);
            }

            //manually update facing states
            //world.setBlock(currentPos,newHeld,2);
            BlockState newFacing = facingState.updateShape(facing.getOpposite(), newHeld, world, facingPos, currentPos);

            if (newFacing != facingState) {
                if (otherTile != null) {
                    otherTile.setHeldBlock(newFacing);
                    otherTile.setChanged();
                } else {
                    world.setBlock(facingPos, newFacing, 2);
                }
            }

            //BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            // world.setBlockAndUpdate(toPos, newState);

            PostType type = getPostType(newHeld);

            if (newHeld != oldHeld) {
                tile.setHeldBlock(newHeld);
                te.setChanged();
            }
            if (newState != state) {
                ((RopeKnotBlockTile) te).recalculateShapes(newState);
            }
            if (type != null) {
                newState = newState.setValue(POST_TYPE, type);
            }
        }

        return newState;
    }


//TODO: fix this not updating mimic block
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(AXIS)) {
                    case X:
                        return state.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return state.setValue(AXIS, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult p_225533_6_) {
        if (player.getItemInHand(hand).getItem() instanceof ShearsItem) {
            if (!world.isClientSide) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof RopeKnotBlockTile) {
                    popResource(world, pos, new ItemStack(ModRegistry.ROPE_ITEM.get()));
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, SoundCategory.PLAYERS, 0.8F, 1.3F);
                    world.setBlock(pos, ((IBlockHolder) te).getHeldBlock(), 3);
                }
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }


    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof RopeKnotBlockTile) {
            BlockState mimic = ((IBlockHolder) te).getHeldBlock();
            return mimic.getBlock().getPickBlock(state, target, world, pos, player);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    public static @Nullable PostType getPostType(BlockState state) {

        PostType type = null;
        //if (state.getBlock().hasTileEntity(state)) return type;
        if (state.is(ModTags.POSTS)) {
            type = PostType.POST;
        } else if (state.is(ModTags.PALISADES) || (CompatHandler.deco_blocks && DecoBlocksCompatRegistry.isPalisade(state))) {
            type = PostType.PALISADE;
        } else if (state.is(ModTags.WALLS)) {
            if ((state.getBlock() instanceof WallBlock) && !state.getValue(WallBlock.UP)) {
                type = PostType.PALISADE;
            } else {
                type = PostType.WALL;
            }
        } else if (state.is(ModTags.BEAMS)) {
            if (state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED)) {
                type = null;
            } else {
                type = PostType.BEAM;
            }
        }

        return type;
    }

    public static @Nullable BlockState convertToRopeKnot(BlockProperties.PostType type, BlockState state, World world, BlockPos pos) {
        Direction.Axis axis = Direction.Axis.Y;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            axis = state.getValue(BlockStateProperties.AXIS);
        }
        BlockState newState = ModRegistry.ROPE_KNOT.get().defaultBlockState()
                .setValue(AXIS, axis).setValue(POST_TYPE, type);
        newState = Block.updateFromNeighbourShapes(newState, world, pos);


        if (!world.setBlock(pos, newState, 0)) {
            return null;
        }

        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof RopeKnotBlockTile) {
            ((IBlockHolder) te).setHeldBlock(state);
            te.setChanged();
        }
        newState.updateNeighbourShapes(world, pos, 2 | Constants.BlockFlags.RERENDER_MAIN_THREAD);
        return newState;
    }


}
