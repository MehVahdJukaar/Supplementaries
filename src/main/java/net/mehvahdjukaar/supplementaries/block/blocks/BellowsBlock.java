package net.mehvahdjukaar.supplementaries.block.blocks;

import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import net.mehvahdjukaar.supplementaries.block.tiles.BellowsBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BellowsBlock extends Block implements EntityBlock {

    private static final VoxelShape DEFAULT_SHAPE = Shapes.create(Shapes.block().bounds().inflate(0.1f));
    private static final Float2ObjectAVLTreeMap<VoxelShape> SHAPES_Y_CACHE = new Float2ObjectAVLTreeMap<>();
    private static final Float2ObjectAVLTreeMap<VoxelShape> SHAPES_X_Z_CACHE = new Float2ObjectAVLTreeMap<>();

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public BellowsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWER, 0));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BellowsBlockTile) {
            float height = ((BellowsBlockTile) te).height;
            //3 digit
            height = (float) (Math.round(height * 1000.0) / 1000.0);

            if (state.getValue(FACING).getAxis() == Direction.Axis.Y) {
                return SHAPES_Y_CACHE.computeIfAbsent(height, BellowsBlock::createVoxelShapeY);
            } else {
                return SHAPES_X_Z_CACHE.computeIfAbsent(height, BellowsBlock::createVoxelShapeXZ);
            }
        }
        return Shapes.block();
    }

    public static VoxelShape createVoxelShapeY(float height) {
        return Shapes.box(0, 0, -height, 1, 1, 1 + height);
    }

    public static VoxelShape createVoxelShapeXZ(float height) {
        return Shapes.box(0, -height, 0, 1, 1 + height, 1);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER);
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
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, Level world, BlockPos pos) {
        int newpower = world.getBestNeighborSignal(pos);
        int currentpower = state.getValue(POWER);
        // on-off
        if (newpower != currentpower) {
            world.setBlock(pos, state.setValue(POWER, newpower), 2 | 4);
            //returns if state changed
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BellowsBlockTile();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return EntityBlock.super.getTicker(pLevel, pState, pBlockEntityType);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BellowsBlockTile) ((BellowsBlockTile) te).onSteppedOn(entityIn);
    }

    @Override
    public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BellowsBlockTile) ((BellowsBlockTile) te).onSteppedOn(entityIn);
    }
}