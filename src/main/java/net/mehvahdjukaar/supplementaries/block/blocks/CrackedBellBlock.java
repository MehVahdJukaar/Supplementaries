package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.CrackedBellBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBellConnection.BellConnection;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BellAttachment;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class CrackedBellBlock extends FallingBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final EnumProperty<BellAttachment> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<BellConnection> BELL_CONNECTION = BlockProperties.BELL_CONNECTION;


    private static final VoxelShape BELL_TOP_SHAPE = Block.box(4.0D, 6.0D, 3.0D, 12.0D, 12.0D, 12.0D);
    private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
    private static final VoxelShape FLOOR_SHAPE = VoxelShapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
    private static final VoxelShape BELL_SHAPE = FLOOR_SHAPE.move(0,0.0625,0);

    private static final VoxelShape NORTH_SOUTH_BETWEEN = VoxelShapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 16.0D));
    private static final VoxelShape EAST_WEST_BETWEEN = VoxelShapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
    private static final VoxelShape TO_WEST = VoxelShapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 13.0D, 15.0D, 9.0D));
    private static final VoxelShape TO_EAST = VoxelShapes.or(BELL_SHAPE, Block.box(3.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
    private static final VoxelShape TO_NORTH = VoxelShapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 13.0D));
    private static final VoxelShape TO_SOUTH = VoxelShapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 3.0D, 9.0D, 15.0D, 16.0D));
    private static final VoxelShape CEILING_SHAPE = VoxelShapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 7.0D, 9.0D, 16.0D, 9.0D));

    public CrackedBellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(BELL_CONNECTION,BellConnection.NONE)
                .setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachment.FLOOR).setValue(POWERED, false));
    }

    @Override
    public int getDustColor(BlockState p_189876_1_, IBlockReader p_189876_2_, BlockPos p_189876_3_) {
        return 0x454748;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int index, int data) {
        super.triggerEvent(state, world, pos, index, data);
        TileEntity tileentity = world.getBlockEntity(pos);
        return tileentity != null && tileentity.triggerEvent(index, data);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moving) {
        boolean flag = world.hasNeighborSignal(pos);
        if (flag != state.getValue(POWERED)) {
            if (flag) {
                this.attemptToRing(world, pos, null);
            }
            world.setBlock(pos, state.setValue(POWERED, flag), 3);
        }
    }


    @Override
    public void onProjectileHit(World world, BlockState state, BlockRayTraceResult hit, ProjectileEntity projectile) {
        Entity entity = projectile.getOwner();
        PlayerEntity playerentity = entity instanceof PlayerEntity ? (PlayerEntity)entity : null;
        this.onHit(world, state, hit, playerentity);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return this.onHit(world, state, hit, player) ? ActionResultType.sidedSuccess(world.isClientSide) : ActionResultType.PASS;
    }

    public boolean onHit(World world, BlockState state, BlockRayTraceResult hit, @Nullable PlayerEntity player) {
        Direction direction = hit.getDirection();
        BlockPos blockpos = hit.getBlockPos();
        boolean flag = this.isProperHit(state, direction, hit.getLocation().y - (double)blockpos.getY());
        if (flag) {
            boolean flag1 = this.attemptToRing(world, blockpos, direction);
            if (flag1 && player != null) {
                //TODO: add stats
                player.awardStat(Stats.BELL_RING);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isProperHit(BlockState state, Direction dir, double p_220129_3_) {
        if (dir.getAxis() != Direction.Axis.Y && !(p_220129_3_ > (double)0.8124F)) {
            Direction direction = state.getValue(FACING);
            BellAttachment bellattachment = state.getValue(ATTACHMENT);
            switch(bellattachment) {
                case SINGLE_WALL:
                case DOUBLE_WALL:
                    return direction.getAxis() != dir.getAxis();
                case CEILING:
                    return true;
            }
        }
        return false;
    }

    public boolean attemptToRing(World world, BlockPos pos, @Nullable Direction direction) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (!world.isClientSide && tileentity instanceof CrackedBellBlockTile) {
            if (direction == null) {
                direction = world.getBlockState(pos).getValue(FACING);
            }

            ((CrackedBellBlockTile)tileentity).onHit(direction);
            world.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundCategory.BLOCKS, 2.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Direction direction = state.getValue(FACING);
        BellAttachment bellattachment = state.getValue(ATTACHMENT);
        if (bellattachment == BellAttachment.FLOOR) {
            return FLOOR_SHAPE;
        }
        else if (bellattachment == BellAttachment.CEILING) {
            return CEILING_SHAPE;
        } else if (bellattachment == BellAttachment.DOUBLE_WALL) {
            return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_BETWEEN : NORTH_SOUTH_BETWEEN;
        } else if (direction == Direction.NORTH) {
            return TO_NORTH;
        } else if (direction == Direction.SOUTH) {
            return TO_SOUTH;
        } else {
            return direction == Direction.EAST ? TO_EAST : TO_WEST;
        }
    }



    @Override
    public BlockRenderType getRenderShape(BlockState p_149645_1_) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        //return this.defaultBlockState().setValue(WATERLOGGED, water);

        Direction direction = context.getClickedFace();
        BlockPos blockpos = context.getClickedPos();
        World world = context.getLevel();
        Direction.Axis direction$axis = direction.getAxis();
        if (direction$axis == Direction.Axis.Y) {
            BlockState blockstate = this.defaultBlockState().setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachment.CEILING : BellAttachment.FLOOR).setValue(FACING, context.getHorizontalDirection());
            if (blockstate.canSurvive(context.getLevel(), blockpos)) {
                return blockstate;
            }
        } else {
            boolean flag = direction$axis == Direction.Axis.X && world.getBlockState(blockpos.west()).isFaceSturdy(world, blockpos.west(), Direction.EAST) && world.getBlockState(blockpos.east()).isFaceSturdy(world, blockpos.east(), Direction.WEST) || direction$axis == Direction.Axis.Z && world.getBlockState(blockpos.north()).isFaceSturdy(world, blockpos.north(), Direction.SOUTH) && world.getBlockState(blockpos.south()).isFaceSturdy(world, blockpos.south(), Direction.NORTH);
            BlockState blockstate1 = this.defaultBlockState().setValue(FACING, direction.getOpposite()).setValue(ATTACHMENT, flag ? BellAttachment.DOUBLE_WALL : BellAttachment.SINGLE_WALL);
            if (blockstate1.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate1;
            }

            boolean flag1 = world.getBlockState(blockpos.below()).isFaceSturdy(world, blockpos.below(), Direction.UP);
            blockstate1 = blockstate1.setValue(ATTACHMENT, flag1 ? BellAttachment.FLOOR : BellAttachment.CEILING);
            if (blockstate1.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate1;
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState p_196271_3_, IWorld world, BlockPos pos, BlockPos p_196271_6_) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        BellAttachment bellattachment = state.getValue(ATTACHMENT);
        Direction direction = getConnectedDirection(state).getOpposite();
        if (direction == dir && !state.canSurvive(world, pos) && bellattachment != BellAttachment.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (dir.getAxis() == state.getValue(FACING).getAxis()) {
                if (bellattachment == BellAttachment.DOUBLE_WALL && !p_196271_3_.isFaceSturdy(world, p_196271_6_, dir)) {
                    return state.setValue(ATTACHMENT, BellAttachment.SINGLE_WALL).setValue(FACING, dir.getOpposite());
                }

                if (bellattachment == BellAttachment.SINGLE_WALL && direction.getOpposite() == dir && p_196271_3_.isFaceSturdy(world, p_196271_6_, state.getValue(FACING))) {
                    return state.setValue(ATTACHMENT, BellAttachment.DOUBLE_WALL);
                }
            }

            return super.updateShape(state, dir, p_196271_3_, world, pos, p_196271_6_);
        }
    }

    public static boolean canFall(BlockState state, BlockPos pos, IWorld world){
        Direction direction = getConnectedDirection(state).getOpposite();
        boolean isAttached;
        switch (direction){
            case UP:
                isAttached = RopeBlock.isSupportingCeiling(pos.above(),world);
                break;
            case DOWN:
                isAttached = !(world.isEmptyBlock(pos.below()) || isFree(world.getBlockState(pos.below())));
                break;
            default:
                isAttached = HorizontalFaceBlock.canAttach(world, pos, direction);
                break;
        }
        return !isAttached && pos.getY() >= 0;
    }


    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (canFall(state, pos, world)) {
            FallingBlockEntity fallingblockentity = new FallingBlockEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, world.getBlockState(pos).setValue(ATTACHMENT,BellAttachment.FLOOR));
            this.falling(fallingblockentity);
            world.addFreshEntity(fallingblockentity);
        }
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(true);
    }

    @Override
    public void onLand(World world, BlockPos pos, BlockState state, BlockState onState, FallingBlockEntity entity) {
        if (!entity.isSilent()) {
            world.levelEvent(1031, pos, 0);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    private static Direction getConnectedDirection(BlockState p_220131_0_) {
        switch(p_220131_0_.getValue(ATTACHMENT)) {
            case FLOOR:
                return Direction.UP;
            case CEILING:
                return Direction.DOWN;
            default:
                return p_220131_0_.getValue(FACING).getOpposite();
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED, WATERLOGGED, BELL_CONNECTION);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CrackedBellBlockTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        return false;
    }

}
