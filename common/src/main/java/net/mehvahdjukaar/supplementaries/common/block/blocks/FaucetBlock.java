package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.FluidsUtil;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FaucetBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE_NORTH = Block.box(5, 5, 5, 11, 15, 16);
    protected static final VoxelShape SHAPE_SOUTH = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_WEST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);
    protected static final VoxelShape SHAPE_EAST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_NORTH_JAR = Block.box(5, 0, 5, 11, 10, 16);
    protected static final VoxelShape SHAPE_SOUTH_JAR = Utils.rotateVoxelShape(SHAPE_NORTH_JAR, Direction.SOUTH);
    protected static final VoxelShape SHAPE_WEST_JAR = Utils.rotateVoxelShape(SHAPE_NORTH_JAR, Direction.WEST);
    protected static final VoxelShape SHAPE_EAST_JAR = Utils.rotateVoxelShape(SHAPE_NORTH_JAR, Direction.EAST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_WATER = ModBlockProperties.HAS_WATER;
    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL_0_7;
    public static final BooleanProperty HAS_JAR = ModBlockProperties.HAS_JAR;

    public FaucetBlock(Properties properties) {
        super(properties.lightLevel(s->s.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_JAR, false).setValue(FACING, Direction.NORTH)
                .setValue(ENABLED, false).setValue(POWERED, false)
                .setValue(HAS_WATER, false).setValue(WATERLOGGED, false).setValue(LIGHT_LEVEL, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(HAS_JAR)) {
            return switch (state.getValue(FACING)) {
                default -> SHAPE_NORTH_JAR;
                case SOUTH -> SHAPE_SOUTH_JAR;
                case EAST -> SHAPE_EAST_JAR;
                case WEST -> SHAPE_WEST_JAR;
            };
        } else {
            return switch (state.getValue(FACING)) {
                default -> SHAPE_NORTH;
                case SOUTH -> SHAPE_SOUTH;
                case EAST -> SHAPE_EAST;
                case WEST -> SHAPE_WEST;
            };
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        boolean enabled = state.getValue(ENABLED);

        float f = enabled ? 1F : 1.2F;
        worldIn.playSound(null, pos, ModSounds.FAUCET.get(), SoundSource.BLOCKS, 1F, f);

        worldIn.gameEvent(player, enabled ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
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
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if (facing == Direction.DOWN) {
            boolean canConnectDown = canConnect(facingState, worldIn, facingPos, facing.getOpposite());
            //boolean water = canConnectDown?stateIn.getValue(HAS_WATER)&&this.isSpecialTankBelow(facingState): updateTileFluid(stateIn,currentPos,worldIn);
            return stateIn.setValue(HAS_JAR, canConnectDown);
        }
        if (facing == stateIn.getValue(FACING).getOpposite()) {
            boolean hasWater = updateTileFluid(stateIn, currentPos, worldIn);
            return stateIn.setValue(HAS_WATER, hasWater);
        }
        return stateIn;
    }

    //returns false if no color (water)
    public boolean updateTileFluid(BlockState state, BlockPos pos, LevelAccessor world) {
        if (world.getBlockEntity(pos) instanceof FaucetBlockTile tile && world instanceof Level level) {
            return tile.updateContainedFluidVisuals(level, pos, state);
        }
        return false;
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (world.getBlockEntity(pos) instanceof FaucetBlockTile tile && world instanceof Level level) {
            boolean water = tile.updateContainedFluidVisuals(level, pos, state);
            if (state.getValue(HAS_WATER) != water) {
                level.setBlock(pos, state.setValue(HAS_WATER, water), 2);
            }
        }
    }

    private boolean canConnect(BlockState downState, LevelAccessor world, BlockPos pos, Direction dir) {
        if (downState.getBlock() instanceof JarBlock) return true;
        else if (downState.is(ModTags.POURING_TANK)) return false;
        else if (downState.hasProperty(BlockStateProperties.LEVEL_HONEY)) return true;
        return world instanceof Level  level && FluidsUtil.hasFluidHandler(level, pos, dir);
    }

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
        if (b instanceof ConcretePowderBlock concretePowderBlock) {
            world.setBlock(pos, concretePowderBlock.concrete, 2 | 16);
        }else if(b instanceof SugarBlock){
            world.removeBlock(pos, false);
        }
    }

    public boolean isOpen(BlockState state) {
        return (state.getValue(BlockStateProperties.POWERED) ^ state.getValue(BlockStateProperties.ENABLED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED, POWERED, HAS_WATER, HAS_JAR, WATERLOGGED, LIGHT_LEVEL);
    }

    //TODO: fix water faucet connecting on rotation

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

        boolean powered = world.hasNeighborSignal(pos);

        return this.defaultBlockState().setValue(FACING, dir)
                .setValue(HAS_JAR, hasJar).setValue(WATERLOGGED, water).setValue(POWERED, powered);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
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
            world.addParticle(ModParticles.DRIPPING_LIQUID.get(), x, y, z, r, g, b);
        }
    }

    //only client
    public int getTileParticleColor(BlockPos pos, Level world) {
        if (world.getBlockEntity(pos) instanceof FaucetBlockTile te)
            return te.tempFluidHolder.getParticleColor(world, pos);
        return 0x423cf7;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FaucetBlockTile(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, ModRegistry.FAUCET_TILE.get(), pLevel.isClientSide ? null : FaucetBlockTile::tick);
    }
}

