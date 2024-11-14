package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FlowerBoxBlock extends WaterBlock implements EntityBlock {

    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 6.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 10.0D, 16.0D, 6.0D, 16.0D);

    protected static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 6.0D, 6.0D, 16.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(10.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);


    protected static final VoxelShape SHAPE_NORTH_FLOOR = Block.box(0.0D, 0.0D, 5.0D, 16.0D, 6.0D, 11.0D);

    protected static final VoxelShape SHAPE_WEST_FLOOR = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 6.0D, 16.0D);

    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL_0_15;
    public static final EnumProperty<AttachFace> ATTACHMENT = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FlowerBoxBlock(Properties properties) {
        super(properties.lightLevel((s) -> s.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(ATTACHMENT, AttachFace.WALL).setValue(LIGHT_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ATTACHMENT, LIGHT_LEVEL);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getClickedFace();
        AttachFace attachface = switch (dir) {
            case UP -> AttachFace.FLOOR;
            case DOWN -> AttachFace.CEILING;
            default -> AttachFace.WALL;
        };
        BlockState blockstate = super.getStateForPlacement(context);
        return blockstate.setValue(ATTACHMENT, attachface)
                .setValue(FACING, dir.getAxis().isVertical() ? context.getHorizontalDirection().getOpposite() : dir);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction dir = switch (state.getValue(ATTACHMENT)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            default -> state.getValue(FACING).getOpposite();
        };

        return level.getBlockState(pos.relative(dir)).isSolid();
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!this.canSurvive(stateIn, worldIn, currentPos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FlowerBoxBlockTile tile) {
            int ind = getIndex(state, pos, hitResult);
            return tile.interactWithPlayerItem(player, hand, stack, ind);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static int getIndex(BlockState state, BlockPos pos, BlockHitResult hit) {
        if (CommonConfigs.Building.FLOWER_BOX_SIMPLE_MODE.get()) return 1;
        int ind;

        Direction dir = state.getValue(FACING);
        Vec3 v = hit.getLocation();
        v = v.subtract(pos.getX() - 1d, 0, pos.getZ() - 1d);

        if (dir.getAxis() == Direction.Axis.X) {
            double normalizedZ = Math.abs((v.z) % 1d);
            if (v.z >= 2) ind = 2;
            else ind = (int) (normalizedZ / (1 / 3d));
            if (dir.getStepX() < 0) ind = 2 - ind;
        } else {
            double normalizedX = Math.abs((v.x) % 1d);
            if (v.x >= 2) ind = 2;
            else ind = (int) (normalizedX / (1 / 3d));
            if (dir.getStepZ() > 0) ind = 2 - ind;
        }
        return ind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FlowerBoxBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean wall = state.getValue(ATTACHMENT) == AttachFace.WALL;
        return switch (state.getValue(FACING)) {
            case SOUTH -> wall ? SHAPE_SOUTH : SHAPE_NORTH_FLOOR;
            case EAST -> wall ? SHAPE_EAST : SHAPE_WEST_FLOOR;
            case WEST -> wall ? SHAPE_WEST : SHAPE_WEST_FLOOR;
            default -> wall ? SHAPE_NORTH : SHAPE_NORTH_FLOOR;
        };
    }

    @ForgeOverride
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        if (target instanceof BlockHitResult hit && hit.getDirection() == Direction.UP) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                ItemStack i = tile.getItem(getIndex(state, pos, hit));
                if (!i.isEmpty()) return i;
            }
        }
        return super.getCloneItemStack(world, pos, state);
    }
}