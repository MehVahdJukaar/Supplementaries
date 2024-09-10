package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.entities.SlimeBallEntity;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AwningBlock extends WaterBlock implements IColored {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    public static final BooleanProperty SLANTED = ModBlockProperties.SLANTED;
    protected static final VoxelShape BOTTOM_INTERACTION = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape BOTTOM_COLLISION = Block.box(0.0, 1.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_INTERACTION = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape TOP_COLLISION = Block.box(0.0, 9.0, 0.0, 16.0, 16.0, 16.0);

    private final DyeColor color;

    public AwningBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BOTTOM, false)
                .setValue(SLANTED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return !state.canSurvive(level, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos behind = pos.relative(direction.getOpposite());
        BlockState behindState = level.getBlockState(behind);

        if (behindState.isSolid()) return true;

        if (behindState.getBlock() instanceof AwningBlock) {
            if (!behindState.getValue(SLANTED)) {
                return true;
            }
            if (behindState.getValue(SLANTED) && state.getValue(BOTTOM)) {
                return true;
            }
        }

        BlockState behindAbove = level.getBlockState(behind.above());
        if (behindAbove.getBlock() instanceof AwningBlock) {
            if (behindAbove.getValue(SLANTED)) {
                return true;
            }
        }
        BlockState left = level.getBlockState(pos.relative(direction.getClockWise()));
        BlockState right = level.getBlockState(pos.relative(direction.getCounterClockWise()));
        return left.isSolid() || right.isSolid();
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (side.getAxis().isHorizontal() && !state.getValue(SLANTED) &&
                adjacentBlockState.getBlock() instanceof AwningBlock &&
                adjacentBlockState.getValue(BOTTOM) == state.getValue(BOTTOM)) {
            if (adjacentBlockState.getValue(SLANTED)) {
                return state.getValue(FACING) != adjacentBlockState.getValue(FACING);
            }
            return true;
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BOTTOM) ? BOTTOM_INTERACTION : TOP_INTERACTION;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getValue(BOTTOM) ? BOTTOM_COLLISION : TOP_COLLISION;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return  state.getValue(BOTTOM) ? BOTTOM_INTERACTION : TOP_INTERACTION;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        boolean bottom = state.getValue(BOTTOM);
        if (CommonConfigs.Building.AWNING_FALL_THROUGH.get()) {
            if (context.isDescending() || !context.isAbove(bottom ? TOP_COLLISION : BOTTOM_COLLISION,
                    bottom ? pos.below() : pos, false)) {
               return Shapes.empty();
            }
        }
        return super.getCollisionShape(state, level, pos, context);

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SLANTED);
        builder.add(FACING);
        builder.add(BOTTOM);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = super.getStateForPlacement(context);
        Direction face = context.getClickedFace();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        List<Direction> directions = new LinkedList<>(Arrays.stream(context.getNearestLookingDirections()).toList());
        BlockState clickedOn = level.getBlockState(pos.relative(face.getOpposite()));
        boolean slanted = false;

        if (clickedOn.getBlock() instanceof AwningBlock) {
            Direction dir = clickedOn.getValue(FACING).getOpposite();
            if (context.getNearestLookingDirection() == dir) {
                directions.remove(dir);
                directions.add(0, dir);
                if (clickedOn.getValue(SLANTED)) {
                    slanted = true;
                }
            }
        }

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction opposite = direction.getOpposite();

                boolean bottom = face != Direction.DOWN &&
                        (face == Direction.UP || !(context.getClickLocation().y - pos.getY() > 0.5));

                List<BlockPos> behindPos = new ArrayList<>();
                behindPos.add(pos.relative(direction));
                if (!bottom) behindPos.add(pos.relative(direction).above());
                if (!CommonConfigs.Building.AWNING_SLANT.get()) {
                    behindPos.clear();
                }

                for (int i = 0; i < behindPos.size(); i++) {
                    BlockState behindState = level.getBlockState(behindPos.get(i));
                    if (behindState.getBlock() instanceof AwningBlock &&
                            behindState.getValue(SLANTED) &&
                            behindState.getValue(FACING) == direction.getOpposite()) {
                        bottom = i == 0;
                        slanted = true;
                        break;
                    }
                }

                blockState = blockState
                        .setValue(BOTTOM, bottom)
                        .setValue(SLANTED, slanted)
                        .setValue(FACING, opposite);
                if (blockState.canSurvive(level, pos)) {
                    return blockState;
                }
            }
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }


    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce() || entity.getType().is(ModTags.AWNING_BLACKLIST)) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSuppressingBounce() && !entity.getType().is(ModTags.AWNING_BLACKLIST)) {
            Vec3 movement = entity.getDeltaMovement();
            if (movement.y < -0.32f) { //for step height when falling from another awning
                Vector3f normal = getNormalVector(state);

                Vector3f newMovement = movement.toVector3f().reflect(normal);
                entity.setDeltaMovement(new Vec3(newMovement));
            }
            //we need to stop movement downwards. do what update entity after fall on does
            else {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    public static @NotNull Vector3f getNormalVector(BlockState state) {
        if (!state.getValue(SLANTED)){
            return new Vector3f(0, 1, 0);
        }
        Direction dir = state.getValue(FACING);
        Vector3f normal = new Vector3f(0, 1, 0);
        if (state.getValue(SLANTED)) {
            double angleDeg = CommonConfigs.Building.AWNINGS_BOUNCE_ANGLE.get();
            normal.rotate(Axis.XP.rotationDegrees((float) (90 - angleDeg)));
        }
        normal.rotate(Axis.YP.rotationDegrees(-dir.toYRot()));
        return normal;
    }

    @Override
    public @Nullable DyeColor getColor() {
        return color;
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).isEmpty() && CommonConfigs.Building.AWNING_SLANT.get()) {
            level.setBlock(pos, state.cycle(SLANTED), 3);
            boolean isSlanted = state.getValue(SLANTED);
            //TODO: proper sound event
            level.playSound(player, pos, SoundEvents.WOOL_PLACE,
                    SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(player, isSlanted ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

}
