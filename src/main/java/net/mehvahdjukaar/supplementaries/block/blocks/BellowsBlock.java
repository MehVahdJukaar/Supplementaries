package net.mehvahdjukaar.supplementaries.block.blocks;

import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import net.mehvahdjukaar.supplementaries.block.tiles.BellowsBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BellowsBlock extends Block {

    private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.create(VoxelShapes.block().bounds().inflate(0.1f));
    private static final Float2ObjectAVLTreeMap<VoxelShape> SHAPES_Y_CACHE = new Float2ObjectAVLTreeMap<>();
    private static final Float2ObjectAVLTreeMap<VoxelShape> SHAPES_X_Z_CACHE = new Float2ObjectAVLTreeMap<>();

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public BellowsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWER, 0));
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

        TileEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof BellowsBlockTile){
            float height = ((BellowsBlockTile) te).height;
            //3 digit
            height = (float)(Math.round(height * 1000.0) / 1000.0);;
            if(state.getValue(FACING).getAxis() == Direction.Axis.Y){
                return SHAPES_Y_CACHE.computeIfAbsent(height, BellowsBlock::createVoxelShapeY);
            }
            else{
                return SHAPES_X_Z_CACHE.computeIfAbsent(height, BellowsBlock::createVoxelShapeXZ);
            }
        }
        return VoxelShapes.block();
    }

    public static VoxelShape createVoxelShapeY(float height) {
        return VoxelShapes.box(0, 0, -height, 1, 1, 1 + height);
    }

    public static VoxelShape createVoxelShapeXZ(float height) {
        return VoxelShapes.box(0, -height, 0, 1, 1 + height, 1);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        int newpower = world.getBestNeighborSignal(pos);
        int currentpower = state.getValue(POWER);
        // on-off
        if (newpower != currentpower) {
            world.setBlock(pos, state.setValue(POWER, newpower), 2 | 4);
            //returns if state changed
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BellowsBlockTile();
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        TileEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof BellowsBlockTile)((BellowsBlockTile) te).onSteppedOn(entityIn);
    }

    @Override
    public void stepOn(World worldIn, BlockPos pos, Entity entityIn) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof BellowsBlockTile)((BellowsBlockTile) te).onSteppedOn(entityIn);
    }
}