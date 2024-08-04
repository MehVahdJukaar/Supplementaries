package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.common.utils.RopeHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.DecoBlocksCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public abstract class AbstractRopeBlock extends WaterBlock implements IRopeConnection {

    //TODO: make solid when player is not colliding
    public static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 13, 16);
    public static final BooleanProperty KNOT = ModBlockProperties.KNOT;

    private final Map<BlockState, VoxelShape> shapes;

    public AbstractRopeBlock(Properties properties) {
        super(properties);
                this.registerDefaultState(this.stateDefinition.any()
                .setValue(KNOT, false).setValue(WATERLOGGED, false));
        shapes = this.makeShapes();
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shapes.getOrDefault(state.setValue(WATERLOGGED, false), Shapes.block());
    }

    protected abstract Map<BlockState, VoxelShape> makeShapes();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, KNOT);
    }

    @ForgeOverride
    public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
        return hasConnection(Direction.DOWN, state) && (hasConnection(Direction.UP, state) || entity.position().y() - pos.getY() < (13 / 16f));
    }

    public abstract boolean hasConnection(Direction dir, BlockState state);

    public abstract BlockState setConnection(Direction dir, BlockState state, boolean value);

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if(!CommonConfigs.Functional.ROPE_HORIZONTAL.get())return Shapes.empty();
        return ((!hasConnection(Direction.UP, state) && (context.isAbove(COLLISION_SHAPE, pos, true) || !hasConnection(Direction.DOWN, state)))
                || !(context instanceof EntityCollisionContext ec && ec.getEntity() instanceof LivingEntity) ?
                getShape(state, worldIn, pos, context) : Shapes.empty());

    }

    public boolean shouldConnectToDir(BlockState thisState, BlockPos currentPos, LevelReader world, Direction dir) {
        if (dir.getAxis().isHorizontal() && !CommonConfigs.Functional.ROPE_HORIZONTAL.get()) return false;
        BlockPos facingPos = currentPos.relative(dir);
        return this.shouldConnectToFace(thisState, world.getBlockState(facingPos), facingPos, dir, world);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (!worldIn.isClientSide()) {
            worldIn.scheduleTick(currentPos, this, 1);
        }

        if (facing == Direction.UP) {
            stateIn = setConnection(Direction.DOWN, stateIn, shouldConnectToDir(stateIn, currentPos, worldIn, Direction.DOWN));
        }
        stateIn = setConnection(facing, stateIn, shouldConnectToDir(stateIn, currentPos, worldIn, facing));


        if (facing == Direction.DOWN && !worldIn.isClientSide() && CompatHandler.DECO_BLOCKS) {
            DecoBlocksCompat.tryConvertingRopeChandelier(facingState, worldIn, facingPos);
        }
        //if (facing != Direction.UP && !worldIn.isClientSide() && CompatHandler.FARMERS_DELIGHT) {
        //FarmersDelightCompat.tryTomatoLogging(facingState, worldIn, facingPos,true);
        //}

        return stateIn.setValue(KNOT, hasMiddleKnot(stateIn));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean hasWater = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = setConnection(dir, state, shouldConnectToDir(state, pos, world, dir));
        }

        state = state.setValue(WATERLOGGED, hasWater);
        state = state.setValue(KNOT, hasMiddleKnot(state));
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isClientSide) {
            worldIn.scheduleTick(pos, this, 1);
            if (CompatHandler.DECO_BLOCKS) {
                BlockPos down = pos.below();
                DecoBlocksCompat.tryConvertingRopeChandelier(worldIn.getBlockState(down), worldIn, down);
            }
        }
    }

    public boolean hasMiddleKnot(BlockState state) {
        boolean up = hasConnection(Direction.UP, state);
        boolean down = hasConnection(Direction.DOWN, state);
        boolean north = hasConnection(Direction.NORTH, state);
        boolean east = hasConnection(Direction.EAST, state);
        boolean south = hasConnection(Direction.SOUTH, state);
        boolean west = hasConnection(Direction.WEST, state);
        //not inverse
        return !((up && down && !north && !south && !east && !west)
                || (!up && !down && north && south && !east && !west)
                || (!up && !down && !north && !south && east && west));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable().move(Direction.UP);
        BlockState upstate = world.getBlockState(mutable);
        if (upstate.is(this)) {
            return true;
        } else if (IRopeConnection.isSupportingCeiling(mutable, world)) {
            return true;
        }

        if (CommonConfigs.Functional.ROPE_HORIZONTAL.get()) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos facingPos = mutable.setWithOffset(pos, direction);
                BlockState sideState = world.getBlockState(facingPos);
                Block b = sideState.getBlock();
                if (b instanceof AbstractRopeBlock) {
                    return true;
                } else if (shouldConnectToFace(this.defaultBlockState(), sideState, facingPos, direction, world))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        //fire up around me
        for (var dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            if (level.getBlockState(pos.relative(dir)).is(BlockTags.FIRE)) {
                level.scheduleTick(pos.relative(dir), Blocks.FIRE, 2 + level.random.nextInt(1));
                for (var d2 : Direction.Plane.HORIZONTAL) {
                    BlockPos fp = pos.relative(d2);
                    if (BaseFireBlock.canBePlacedAt(level, fp, d2.getOpposite())) {
                        BlockState fireState = BaseFireBlock.getState(level, fp);
                        if (fireState.hasProperty(FireBlock.AGE)) {
                            fireState = fireState.setValue(FireBlock.AGE, 14);
                        }
                        level.setBlockAndUpdate(fp, fireState);
                        level.scheduleTick(pos.relative(dir), Blocks.FIRE, 2 + level.random.nextInt(1));
                    }
                }
                return;
            }
        }
    }

    @ForgeOverride
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 10;
    }

    @ForgeOverride
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 100; //chance to get consumed
    }

    private static boolean findConnectedPulley(Level world, BlockPos pos, Player player, int it, Rotation rot) {
        if (it > 64) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof AbstractRopeBlock) {
            return findConnectedPulley(world, pos.above(), player, it + 1, rot);
        } else if (b instanceof PulleyBlock pulley && it != 0) {
            return pulley.windPulley(state, pos, world, rot, null);
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand
            handIn, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item i = stack.getItem();

        if (i == this.asItem()) {
            if (hit.getDirection().getAxis() == Direction.Axis.Y || hasConnection(Direction.DOWN, state)) {
                //restores sheared
                if (hasConnection(Direction.UP, state) && !hasConnection(Direction.DOWN, state)) {
                    state = setConnection(Direction.DOWN, state, true);
                    world.setBlock(pos, state, 0);
                }
                if (RopeHelper.addRopeDown(pos.below(), world, player, handIn, this)) {
                    SoundType soundtype = state.getSoundType();
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            return InteractionResult.PASS;
        } else if (stack.isEmpty()) {
            if (hasConnection(Direction.UP, state)) {
                if (findConnectedPulley(world, pos, player, 0, player.isShiftKeyDown() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90)) {
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            if (!player.isShiftKeyDown() && handIn == InteractionHand.MAIN_HAND) {
                if (world.getBlockState(pos.below()).getBlock() == this) {
                    if (RopeHelper.removeRopeDown(pos.below(), world, this)) {
                        world.playSound(player, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1, 0.6f);
                        if (!player.getAbilities().instabuild) {
                            ItemsUtil.addStackToExisting(player, new ItemStack(this), true);
                        }
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
        } else if (i instanceof ShearsItem) {
            if (hasConnection(Direction.DOWN, state)) {
                if (!world.isClientSide) {
                    //TODO: proper sound event here
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 0.8F, 1.3F);
                    BlockState newState = setConnection(Direction.DOWN, state, false).setValue(KNOT, true);
                    world.setBlock(pos, newState, 3);
                    //refreshTextures below
                    //world.updateNeighborsAt(pos, newState.getBlock());
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }


    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        if (entityIn instanceof Arrow && !worldIn.isClientSide) {
            worldIn.destroyBlock(pos, true, entityIn);
            //TODO: add proper sound event
            worldIn.playSound(null, pos, SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 1, 1);
        }
    }

    //for culling
    @Override
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(this) || super.skipRendering(pState, pAdjacentBlockState, pSide);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> {
                state = setConnection(Direction.NORTH, state, hasConnection(Direction.SOUTH, state));
                state = setConnection(Direction.EAST, state, hasConnection(Direction.WEST, state));
                state = setConnection(Direction.SOUTH, state, hasConnection(Direction.NORTH, state));
                state = setConnection(Direction.WEST, state, hasConnection(Direction.EAST, state));
                yield state;
            }
            case COUNTERCLOCKWISE_90 -> {
                state = setConnection(Direction.NORTH, state, hasConnection(Direction.EAST, state));
                state = setConnection(Direction.EAST, state, hasConnection(Direction.SOUTH, state));
                state = setConnection(Direction.SOUTH, state, hasConnection(Direction.WEST, state));
                state = setConnection(Direction.WEST, state, hasConnection(Direction.NORTH, state));
                yield state;
            }
            case CLOCKWISE_90 -> {
                state = setConnection(Direction.NORTH, state, hasConnection(Direction.WEST, state));
                state = setConnection(Direction.EAST, state, hasConnection(Direction.NORTH, state));
                state = setConnection(Direction.SOUTH, state, hasConnection(Direction.EAST, state));
                state = setConnection(Direction.WEST, state, hasConnection(Direction.SOUTH, state));
                yield state;
            }
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> {
                state = setConnection(Direction.NORTH, state, hasConnection(Direction.SOUTH, state));
                state = setConnection(Direction.SOUTH, state, hasConnection(Direction.NORTH, state));
                yield state;
            }
            case FRONT_BACK -> {
                state = setConnection(Direction.EAST, state, hasConnection(Direction.WEST, state));
                state = setConnection(Direction.WEST, state, hasConnection(Direction.EAST, state));
                yield state;
            }
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public boolean canSideAcceptConnection(BlockState state, Direction direction) {
        return true;
    }


}
