package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.CrackedBellBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBellConnections.BellConnection;
import net.minecraft.block.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class CrackedBellBlock extends FallingBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<BellConnection> BELL_CONNECTION = BlockProperties.BELL_CONNECTION;


    private static final VoxelShape BELL_TOP_SHAPE = Block.box(4.0D, 3.0D, 4.0D, 12.0D, 12.0D, 12.0D);
    private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
    private static final VoxelShape FLOOR_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
    private static final VoxelShape BELL_SHAPE = FLOOR_SHAPE.move(0,0.0625,0);

    private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 16.0D));
    private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
    private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 13.0D, 15.0D, 9.0D));
    private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
    private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 13.0D));
    private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 3.0D, 9.0D, 15.0D, 16.0D));
    private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 7.0D, 9.0D, 16.0D, 9.0D));

    public CrackedBellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(BELL_CONNECTION,BellConnection.NONE)
                .setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, false));
    }

    @Override
    public int getDustColor(BlockState p_189876_1_, BlockGetter p_189876_2_, BlockPos p_189876_3_) {
        return 0x454748;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int index, int data) {
        super.triggerEvent(state, world, pos, index, data);
        BlockEntity tileentity = world.getBlockEntity(pos);
        return tileentity != null && tileentity.triggerEvent(index, data);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moving) {
        boolean flag = world.hasNeighborSignal(pos);
        if (flag != state.getValue(POWERED)) {
            if (flag) {
                this.attemptToRing(world, pos, null);
            }
            world.setBlock(pos, state.setValue(POWERED, flag), 3);
        }
    }


    @Override
    public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        Entity entity = projectile.getOwner();
        Player playerentity = entity instanceof Player ? (Player)entity : null;
        this.onHit(world, state, hit, playerentity);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.onHit(world, state, hit, player) ? InteractionResult.sidedSuccess(world.isClientSide) : InteractionResult.PASS;
    }

    public boolean onHit(Level world, BlockState state, BlockHitResult hit, @Nullable Player player) {
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
            BellAttachType bellattachment = state.getValue(ATTACHMENT);
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

    public boolean attemptToRing(Level world, BlockPos pos, @Nullable Direction direction) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (!world.isClientSide && tileentity instanceof CrackedBellBlockTile) {
            if (direction == null) {
                direction = world.getBlockState(pos).getValue(FACING);
            }

            ((CrackedBellBlockTile)tileentity).onHit(direction);
            world.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 0.65F);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        BellAttachType bellattachment = state.getValue(ATTACHMENT);
        if (bellattachment == BellAttachType.FLOOR) {
            return FLOOR_SHAPE;
        }
        else if (bellattachment == BellAttachType.CEILING) {
            return CEILING_SHAPE;
        } else if (bellattachment == BellAttachType.DOUBLE_WALL) {
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
    public RenderShape getRenderShape(BlockState p_149645_1_) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        //return this.defaultBlockState().setValue(WATERLOGGED, water);

        Direction direction = context.getClickedFace();
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        Direction.Axis direction$axis = direction.getAxis();
        if (direction$axis == Direction.Axis.Y) {
            BlockState blockstate = this.defaultBlockState().setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR).setValue(FACING, context.getHorizontalDirection());
            if (blockstate.canSurvive(context.getLevel(), blockpos)) {
                return blockstate;
            }
        } else {
            boolean flag = direction$axis == Direction.Axis.X && world.getBlockState(blockpos.west()).isFaceSturdy(world, blockpos.west(), Direction.EAST) && world.getBlockState(blockpos.east()).isFaceSturdy(world, blockpos.east(), Direction.WEST) || direction$axis == Direction.Axis.Z && world.getBlockState(blockpos.north()).isFaceSturdy(world, blockpos.north(), Direction.SOUTH) && world.getBlockState(blockpos.south()).isFaceSturdy(world, blockpos.south(), Direction.NORTH);
            BlockState blockstate1 = this.defaultBlockState().setValue(FACING, direction.getOpposite()).setValue(ATTACHMENT, flag ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
            if (blockstate1.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate1;
            }

            boolean flag1 = world.getBlockState(blockpos.below()).isFaceSturdy(world, blockpos.below(), Direction.UP);
            blockstate1 = blockstate1.setValue(ATTACHMENT, flag1 ? BellAttachType.FLOOR : BellAttachType.CEILING);
            if (blockstate1.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate1;
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState p_196271_3_, LevelAccessor world, BlockPos pos, BlockPos p_196271_6_) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        BellAttachType bellattachment = state.getValue(ATTACHMENT);
        Direction direction = getConnectedDirection(state).getOpposite();
        if (direction == dir && !state.canSurvive(world, pos) && bellattachment != BellAttachType.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (dir.getAxis() == state.getValue(FACING).getAxis()) {
                if (bellattachment == BellAttachType.DOUBLE_WALL && !p_196271_3_.isFaceSturdy(world, p_196271_6_, dir)) {
                    return state.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, dir.getOpposite());
                }

                if (bellattachment == BellAttachType.SINGLE_WALL && direction.getOpposite() == dir && p_196271_3_.isFaceSturdy(world, p_196271_6_, state.getValue(FACING))) {
                    return state.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
                }
            }

            return super.updateShape(state, dir, p_196271_3_, world, pos, p_196271_6_);
        }
    }

    public static boolean canFall(BlockState state, BlockPos pos, LevelAccessor world){
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
                isAttached = FaceAttachedHorizontalDirectionalBlock.canAttach(world, pos, direction);
                break;
        }
        return !isAttached && pos.getY() >= 0;
    }


    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if (canFall(state, pos, world)) {
            FallingBlockEntity fallingblockentity = new FallingBlockEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, world.getBlockState(pos).setValue(ATTACHMENT,BellAttachType.FLOOR));
            this.falling(fallingblockentity);
            world.addFreshEntity(fallingblockentity);
        }
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(true);
    }

    @Override
    public void onLand(Level world, BlockPos pos, BlockState state, BlockState onState, FallingBlockEntity entity) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED, WATERLOGGED, BELL_CONNECTION);
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new CrackedBellBlockTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType pathType) {
        return false;
    }

}
