package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FaucetBlock extends WaterBlock {
    protected static final VoxelShape SHAPE_NORTH = Shapes.box(0.6875D, 0.3125D, 1D, 0.3125D, 0.9375D, 0.3125D);
    protected static final VoxelShape SHAPE_SOUTH = Shapes.box(0.3125D, 0.3125D, 0D, 0.6875D, 0.9375D, 0.6875D);
    protected static final VoxelShape SHAPE_WEST = Shapes.box(1D, 0.3125D, 0.3125D, 0.3125D, 0.9375D, 0.6875D);
    protected static final VoxelShape SHAPE_EAST = Shapes.box(0D, 0.3125D, 0.6875D, 0.6875D, 0.9375D, 0.3125D);
    protected static final VoxelShape SHAPE_NORTH_JAR = Shapes.box(0.6875D, 0, 1D, 0.3125D, 0.625D, 0.3125D);
    protected static final VoxelShape SHAPE_SOUTH_JAR = Shapes.box(0.3125D, 0, 0D, 0.6875D, 0.625D, 0.6875D);
    protected static final VoxelShape SHAPE_WEST_JAR = Shapes.box(1D, 0, 0.3125D, 0.3125D, 0.625D, 0.6875D);
    protected static final VoxelShape SHAPE_EAST_JAR = Shapes.box(0D, 0, 0.6875D, 0.6875D, 0.625D, 0.3125D);

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
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
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
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                BlockHitResult hit) {
        //TODO: add item activated method for bottles
        boolean enabled = state.getValue(ENABLED);
        if (!state.getValue(HAS_JAR) && hit.getLocation().y % 1 <= 0.4375) {
            if (enabled && state.getValue(HAS_WATER)) {
                Direction dir = state.getValue(FACING);
                BlockPos backPos = pos.relative(dir.getOpposite());
                BlockState backState = worldIn.getBlockState(backPos);
                BlockHitResult rayTraceResult = new BlockHitResult(new Vec3(backPos.getX() + 0.5,
                        backPos.getY() + 0.5, backPos.getZ() + 0.5), dir, backPos, false);
                InteractionResult blockResult = backState.use(worldIn, player, handIn, rayTraceResult);
                if (blockResult.consumesAction()) return blockResult;
                InteractionResult itemResult = player.getItemInHand(handIn).getItem().useOn(new UseOnContext(player, handIn, rayTraceResult));
                if (itemResult.consumesAction()) return itemResult;
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }


        float f = enabled ? 0.6F : 0.5F;
        worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
        this.updateBlock(state, worldIn, pos, true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        boolean hasWater = updateTileFluid(state, pos, worldIn);
        if (hasWater != state.getValue(HAS_WATER)) worldIn.setBlockAndUpdate(pos, state.setValue(HAS_WATER, hasWater));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
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
    public boolean updateTileFluid(BlockState state, BlockPos pos, LevelAccessor world) {

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof FaucetBlockTile) {
            return ((FaucetBlockTile) te).updateContainedFluidVisuals(state);
        }
        return false;
    }

    //TODO: redo
    private boolean canConnect(BlockState downState, LevelAccessor world, BlockPos pos, Direction dir) {
        if (downState.getBlock() instanceof JarBlock) return true;
        else if (downState.is(ModTags.POURING_TANK)) return false;
        else if (downState.hasProperty(BlockStateProperties.LEVEL_HONEY)) return true;
        return world instanceof Level && FluidUtil.getFluidHandler((Level) world, pos, dir).isPresent();
    }

    //TODO also fix faucet glass connection shading
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updateBlock(state, world, pos, false);
    }

    public void updateBlock(BlockState state, Level world, BlockPos pos, boolean toggle) {
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

    public void trySolidifyConcrete(BlockPos pos, Level world) {
        Block b = world.getBlockState(pos).getBlock();
        if (b instanceof ConcretePowderBlock)
            world.setBlock(pos, ((ConcretePowderBlock) b).concrete, 2 | 16);
    }


    public boolean isOpen(BlockState state) {
        return (state.getValue(BlockStateProperties.POWERED) ^ state.getValue(BlockStateProperties.ENABLED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
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
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
        boolean flag = this.isOpen(state);
        if (state.getValue(HAS_WATER) && !state.getValue(HAS_JAR)) {
            if (random.nextFloat() > (flag ? 0 : 0.06)) return;
            float d = 0.125f;
            double x = (pos.getX() + 0.5 + d * (random.nextFloat() - 0.5));
            double y = (pos.getY() + 0.25);
            double z = (pos.getZ() + 0.5 + d * (random.nextFloat() - 0.5));
            int color = getTileParticleColor(pos, world);
            //get texture color if color is white
            float r = FastColor.ARGB32.red(color) / 255f;
            float g = FastColor.ARGB32.green(color) / 255f;
            float b = FastColor.ARGB32.blue(color) / 255f;
            world.addParticle(ModRegistry.DRIPPING_LIQUID.get(), x, y, z, r, g, b);

            //world.addParticle(flag?Registry.FALLING_LIQUID.get():Registry.DRIPPING_LIQUID.get(), x, y, z, 0, 1, 0);
        }
    }

    //only client
    public int getTileParticleColor(BlockPos pos, Level world) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof FaucetBlockTile)
            return ((FaucetBlockTile) te).fluidHolder.getParticleColor(world, pos);
        return 0x423cf7;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new FaucetBlockTile();
    }

}

