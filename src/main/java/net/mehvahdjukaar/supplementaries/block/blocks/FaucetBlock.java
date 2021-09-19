package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Random;

public class FaucetBlock extends WaterBlock {
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.box(0.6875D, 0.3125D, 1D, 0.3125D, 0.9375D, 0.3125D);
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.box(0.3125D, 0.3125D, 0D, 0.6875D, 0.9375D, 0.6875D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.box(1D, 0.3125D, 0.3125D, 0.3125D, 0.9375D, 0.6875D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.box(0D, 0.3125D, 0.6875D, 0.6875D, 0.9375D, 0.3125D);
    protected static final VoxelShape SHAPE_NORTH_JAR = VoxelShapes.box(0.6875D, 0, 1D, 0.3125D, 0.625D, 0.3125D);
    protected static final VoxelShape SHAPE_SOUTH_JAR = VoxelShapes.box(0.3125D, 0, 0D, 0.6875D, 0.625D, 0.6875D);
    protected static final VoxelShape SHAPE_WEST_JAR = VoxelShapes.box(1D, 0, 0.3125D, 0.3125D, 0.625D, 0.6875D);
    protected static final VoxelShape SHAPE_EAST_JAR = VoxelShapes.box(0D, 0, 0.6875D, 0.6875D, 0.625D, 0.3125D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_WATER = BlockProperties.HAS_WATER;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;
    public static final BooleanProperty HAS_JAR = BlockProperties.HAS_JAR;
    public static final BooleanProperty EXTENDED = BlockStateProperties.ATTACHED; //glass extension

    public FaucetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_JAR, false).setValue(FACING, Direction.NORTH)
                .setValue(ENABLED, false).setValue(EXTENDED, false).setValue(POWERED, false)
                .setValue(HAS_WATER, false).setValue(WATERLOGGED, false).setValue(LIGHT_LEVEL, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (state.getValue(HAS_JAR)) {
            switch (state.getValue(FACING)) {
                case UP:
                case DOWN:
                case NORTH:
                default:
                    return SHAPE_NORTH_JAR;
                case SOUTH:
                    return SHAPE_SOUTH_JAR;
                case EAST:
                    return SHAPE_EAST_JAR;
                case WEST:
                    return SHAPE_WEST_JAR;
            }
        } else {
            switch (state.getValue(FACING)) {
                case UP:
                case DOWN:
                case NORTH:
                default:
                    return SHAPE_NORTH;
                case SOUTH:
                    return SHAPE_SOUTH;
                case EAST:
                    return SHAPE_EAST;
                case WEST:
                    return SHAPE_WEST;
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        //TODO: add item activated method for bottles
        boolean enabled = state.getValue(ENABLED);
        if (!state.getValue(HAS_JAR) && hit.getLocation().y % 1 <= 0.4375) {
            if (enabled && state.getValue(HAS_WATER)) {
                Direction dir = state.getValue(FACING);
                BlockPos backPos = pos.relative(dir.getOpposite());
                BlockState backState = worldIn.getBlockState(backPos);
                BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vector3d(backPos.getX() + 0.5,
                        backPos.getY() + 0.5, backPos.getZ() + 0.5), dir, backPos, false);
                ActionResultType blockResult = backState.use(worldIn, player, handIn, rayTraceResult);
                if (blockResult.consumesAction()) return blockResult;
                ActionResultType itemResult = player.getItemInHand(handIn).getItem().useOn(new ItemUseContext(player, handIn, rayTraceResult));
                if (itemResult.consumesAction()) return itemResult;
            }
            return ActionResultType.sidedSuccess(worldIn.isClientSide);
        }


        float f = enabled ? 0.6F : 0.5F;
        worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
        this.updateBlock(state, worldIn, pos, true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        boolean hasWater = updateTileFluid(state, pos, worldIn);
        if (hasWater != state.getValue(HAS_WATER)) worldIn.setBlockAndUpdate(pos, state.setValue(HAS_WATER, hasWater));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if (facing == Direction.DOWN) {
            boolean canConnectDown = canConnect(facingState, worldIn, facingPos, facing.getOpposite());
            //boolean water = canConnectDown?stateIn.getValue(HAS_WATER)&&this.isSpecialTankBelow(facingState): updateTileFluid(stateIn,currentPos,worldIn);
            return stateIn.setValue(HAS_JAR, canConnectDown);
        }
        if (facing == stateIn.getValue(FACING).getOpposite()) {
            boolean hasWater = updateTileFluid(stateIn, currentPos, worldIn);
            return stateIn.setValue(EXTENDED, canConnect(facingState, worldIn, facingPos, facing.getOpposite())).setValue(HAS_WATER, hasWater);
        }
        return stateIn;
    }

    //returns false if no color (water)
    public boolean updateTileFluid(BlockState state, BlockPos pos, IWorld world) {

        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof FaucetBlockTile) {
            return ((FaucetBlockTile) te).updateContainedFluidVisuals(state);
        }
        return false;
    }

    //TODO: redo
    private boolean canConnect(BlockState downState, IWorld world, BlockPos pos, Direction dir) {
        if (downState.getBlock() instanceof JarBlock) return true;
        else if (downState.is(ModTags.POURING_TANK)) return false;
        else if (downState.hasProperty(BlockStateProperties.LEVEL_HONEY)) return true;
        return world instanceof World && FluidUtil.getFluidHandler((World) world, pos, dir).isPresent();
    }

    //TODO also fix faucet glass connection shading
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updateBlock(state, world, pos, false);
    }

    public void updateBlock(BlockState state, World world, BlockPos pos, boolean toggle) {
        boolean isPowered = world.hasNeighborSignal(pos);
        if (isPowered != state.getValue(POWERED) || toggle) {
            world.setBlock(pos, state.setValue(POWERED, isPowered).setValue(ENABLED, toggle ^ state.getValue(ENABLED)), 2);
        }

        boolean hasWater = updateTileFluid(state, pos, world);
        if (hasWater != state.getValue(HAS_WATER)) world.setBlockAndUpdate(pos, state.setValue(HAS_WATER, hasWater));


        //handles concrete
        if (state.getValue(ENABLED) ^ toggle ^ isPowered && state.getValue(HAS_WATER)) {
            trySolidifyConcrete(pos.below(), world);
        }
    }

    public void trySolidifyConcrete(BlockPos pos, World world) {
        Block b = world.getBlockState(pos).getBlock();
        if (b instanceof ConcretePowderBlock)
            world.setBlock(pos, ((ConcretePowderBlock) b).concrete, 2 | 16);
    }


    public boolean isOpen(BlockState state) {
        return (state.getValue(BlockStateProperties.POWERED) ^ state.getValue(BlockStateProperties.ENABLED));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED, FACING, ENABLED, POWERED, HAS_WATER, HAS_JAR, WATERLOGGED, LIGHT_LEVEL);
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
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction dir = context.getClickedFace().getAxis() == Direction.Axis.Y ? Direction.NORTH : context.getClickedFace();

        boolean water = world.getFluidState(pos).getType() == Fluids.WATER;
        boolean hasJar = canConnect(world.getBlockState(pos.below()), world, pos.below(), Direction.UP);
        BlockPos backPos = pos.relative(dir.getOpposite());
        boolean jarBehind = canConnect(world.getBlockState(backPos), world, backPos, dir.getOpposite());
        boolean powered = world.hasNeighborSignal(pos);

        return this.defaultBlockState().setValue(FACING, dir).setValue(EXTENDED, jarBehind)
                .setValue(HAS_JAR, hasJar).setValue(WATERLOGGED, water).setValue(POWERED, powered);
    }

    //TODO: maybe remove haswater state
    //TODO: add luminance
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        boolean flag = this.isOpen(state);
        if (state.getValue(HAS_WATER) && !state.getValue(HAS_JAR)) {
            if (random.nextFloat() > (flag ? 0 : 0.06)) return;
            float d = 0.125f;
            double x = (pos.getX() + 0.5 + d * (random.nextFloat() - 0.5));
            double y = (pos.getY() + 0.25);
            double z = (pos.getZ() + 0.5 + d * (random.nextFloat() - 0.5));
            int color = getTileParticleColor(pos, world);
            //get texture color if color is white
            float r = ColorHelper.PackedColor.red(color) / 255f;
            float g = ColorHelper.PackedColor.green(color) / 255f;
            float b = ColorHelper.PackedColor.blue(color) / 255f;
            world.addParticle(ModRegistry.DRIPPING_LIQUID.get(), x, y, z, r, g, b);

            //world.addParticle(flag?Registry.FALLING_LIQUID.get():Registry.DRIPPING_LIQUID.get(), x, y, z, 0, 1, 0);
        }
    }

    //only client
    public int getTileParticleColor(BlockPos pos, World world) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof FaucetBlockTile)
            return ((FaucetBlockTile) te).fluidHolder.getParticleColor(world, pos);
        return 0x423cf7;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FaucetBlockTile();
    }

}

