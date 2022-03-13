package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils.PlayerLessContext;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.mehvahdjukaar.supplementaries.common.items.ItemsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.decorativeblocks.RopeChandelierBlock;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPistonPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

public class RopeBlock extends WaterBlock {
    private final Map<BlockState, VoxelShape> SHAPES_MAP = new HashMap<>();

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
    public static final BooleanProperty KNOT = BlockProperties.KNOT;

    public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
        directions.put(Direction.UP, UP);
        directions.put(Direction.DOWN, DOWN);
    });

    public RopeBlock(Properties properties) {
        super(properties);
        this.makeShapes();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP, true).setValue(DOWN, true).setValue(KNOT, false).setValue(DISTANCE, 7).setValue(WATERLOGGED, false)
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false));
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(DISTANCE, 0).setValue(WATERLOGGED, false), Shapes.block());
    }

    //oh boy 32k shapes. 2k by removing water and distance lol
    protected void makeShapes() {
        VoxelShape down = Block.box(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.box(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.box(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.box(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.box(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.box(6, 9, 6, 16, 13, 10);
        VoxelShape knot = Block.box(6, 9, 6, 10, 13, 10);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.getValue(WATERLOGGED) || state.getValue(DISTANCE) != 0) continue;
            VoxelShape v = Shapes.empty();
            if (state.getValue(KNOT)) v = Shapes.or(knot);
            if (state.getValue(DOWN)) v = Shapes.or(v, down);
            if (state.getValue(UP)) v = Shapes.or(v, up);
            if (state.getValue(NORTH)) v = Shapes.or(v, north);
            if (state.getValue(SOUTH)) v = Shapes.or(v, south);
            if (state.getValue(WEST)) v = Shapes.or(v, west);
            if (state.getValue(EAST)) v = Shapes.or(v, east);
            v = v.optimize();
            boolean flag = true;
            for (VoxelShape existing : this.SHAPES_MAP.values()) {
                if (existing.equals(v)) {
                    this.SHAPES_MAP.put(state, existing);
                    flag = false;
                    break;
                }
            }
            if (flag) this.SHAPES_MAP.put(state, v);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED, DISTANCE, KNOT);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
        return state.getValue(DOWN) && (state.getValue(UP) || entity.position().y() - pos.getY() < (13 / 16f));
    }

    //TODO: make solid when player is not colliding
    private static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 13, 16);

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ((!state.getValue(UP) && (context.isAbove(COLLISION_SHAPE, pos, true) || !state.getValue(DOWN)))
                || !(context instanceof EntityCollisionContext ec && ec.getEntity() instanceof LivingEntity) ?
                getShape(state, worldIn, pos, context) : Shapes.empty());

    }

    public static boolean shouldConnectToDir(BlockState thisState, BlockPos currentPos, LevelReader world, Direction dir) {
        BlockPos facingPos = currentPos.relative(dir);
        return shouldConnectToFace(thisState, world.getBlockState(facingPos), facingPos, dir, world);
    }

    public static boolean shouldConnectToFace(BlockState thisState, BlockState facingState, BlockPos facingPos, Direction dir, LevelReader world) {
        Block thisBlock = thisState.getBlock();
        Block b = facingState.getBlock();
        boolean isKnot = thisBlock == ModRegistry.ROPE_KNOT.get();
        boolean isVerticalKnot = isKnot && thisState.getValue(RopeKnotBlock.AXIS) == Direction.Axis.Y;

        switch (dir) {
            case UP -> {
                if (isVerticalKnot) return false;
                return RopeBlock.isSupportingCeiling(facingState, facingPos, world);
            }
            case DOWN -> {
                if (isVerticalKnot) return false;
                return RopeBlock.isSupportingCeiling(facingPos.above(2), world) || RopeBlock.canConnectDown(facingState);
            }
            default -> {
                if(ServerConfigs.cached.ROPE_UNRESTRICTED && facingState.isFaceSturdy(world, facingPos, dir.getOpposite())){
                    return true;
                }
                if (facingState.is(ModRegistry.ROPE_KNOT.get())) {
                    return thisBlock != b && (dir.getAxis() == Direction.Axis.Y || facingState.getValue(RopeKnotBlock.AXIS) == Direction.Axis.Y);
                } else if (isKnot && !isVerticalKnot) {
                    return false;
                }
                return b == ModRegistry.ROPE.get();
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (!worldIn.isClientSide()) {
            worldIn.scheduleTick(currentPos, this, 1);
        }

        if (facing == Direction.UP) {
            stateIn = stateIn.setValue(DOWN, shouldConnectToDir(stateIn, currentPos, worldIn, Direction.DOWN));
        }
        stateIn = stateIn.setValue(FACING_TO_PROPERTY_MAP.get(facing), shouldConnectToDir(stateIn, currentPos, worldIn, facing));


        if (facing == Direction.DOWN && !worldIn.isClientSide() && CompatHandler.deco_blocks) {
            RopeChandelierBlock.tryConverting(facingState, worldIn, facingPos);
        }

        return stateIn.setValue(KNOT, hasMiddleKnot(stateIn));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean hasWater = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = state.setValue(FACING_TO_PROPERTY_MAP.get(dir), shouldConnectToDir(state, pos, world, dir));
        }

        state = state.setValue(WATERLOGGED, hasWater);
        state = state.setValue(KNOT, hasMiddleKnot(state)).setValue(DISTANCE, this.getDistance(world, pos));
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isClientSide) {
            worldIn.scheduleTick(pos, this, 1);
            if (CompatHandler.deco_blocks) {
                BlockPos down = pos.below();
                RopeChandelierBlock.tryConverting(worldIn.getBlockState(down), worldIn, down);
            }
        }
    }

    public static boolean hasMiddleKnot(BlockState state) {
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        //not inverse
        return !((up && down && !north && !south && !east && !west)
                || (!up && !down && north && south && !east && !west)
                || (!up && !down && !north && !south && east && west));

    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return (this.getDistance(worldIn, pos) < 7);
        //return!(!state.get(UP)&&state.get(NORTH).isNone()&&state.get(SOUTH).isNone()&&state.get(EAST).isNone()&&state.get(WEST).isNone());
    }

    public static boolean isSupportingCeiling(BlockState facingState, BlockPos pos, LevelReader world) {
        Block b = facingState.getBlock();
        return canSupportCenter(world, pos, Direction.DOWN) || facingState.is(ModTags.ROPE_SUPPORT_TAG) ||
                (facingState.is(ModRegistry.ROPE_KNOT.get()) && facingState.getValue(RopeKnotBlock.AXIS) != Direction.Axis.Y);
    }

    public static boolean isSupportingCeiling(BlockPos pos, LevelReader world) {
        return isSupportingCeiling(world.getBlockState(pos), pos, world);
    }

    public static boolean canConnectDown(BlockPos currentPos, LevelReader world) {
        BlockState state = world.getBlockState(currentPos.below());
        return canConnectDown(state);
    }

    public static boolean canConnectDown(BlockState downState) {
        Block b = downState.getBlock();
        return (downState.is(ModRegistry.ROPE.get()) || downState.is(ModTags.ROPE_HANG_TAG)
                || (downState.is(ModRegistry.ROPE_KNOT.get()) && downState.getValue(RopeKnotBlock.AXIS) != Direction.Axis.Y)
                || (downState.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE) && downState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING)
                || (b instanceof ChainBlock && downState.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y)
                || (downState.hasProperty(BlockStateProperties.HANGING) && downState.getValue(BlockStateProperties.HANGING)));
    }

    public int getDistance(LevelReader world, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable().move(Direction.UP);
        BlockState blockstate = world.getBlockState(mutable);
        int i = 7;
        if (blockstate.is(this)) {
            if (blockstate.getValue(DOWN) || !blockstate.getValue(UP)) {
                i = blockstate.getValue(DISTANCE);
            }
        } else if (isSupportingCeiling(mutable, world)) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = mutable.setWithOffset(pos, direction);
            BlockState sideState = world.getBlockState(facingPos);
            Block b = sideState.getBlock();
            if (b instanceof RopeBlock) {
                i = Math.min(i, sideState.getValue(DISTANCE) + 1);
                if (i == 1) {
                    break;
                }
            } else if (shouldConnectToFace(this.defaultBlockState(), sideState, facingPos, direction, world)) i = 0;
        }

        return i;
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        int i = this.getDistance(worldIn, pos);
        BlockState blockstate = state.setValue(DISTANCE, i);
        if (i == 7) {
            worldIn.destroyBlock(pos, true);
        } else if (state != blockstate) {
            worldIn.setBlock(pos, blockstate, 3);
        }
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    public static boolean findAndRingBell(Level world, BlockPos pos, Player player, int it, Predicate<BlockState> predicate) {

        if (it > ServerConfigs.cached.BELL_CHAIN_LENGTH) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (predicate.test(state)) {
            return findAndRingBell(world, pos.above(), player, it + 1, predicate);
        } else if (b instanceof BellBlock && it != 0) {
            //boolean success = CommonUtil.tryRingBell(Block b, world, pos, state.getValue(BellBlock.FACING).getClockWise());
            BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    state.getValue(BellBlock.FACING).getClockWise(), pos, true);
            //if (success && player != null) {//player.awardStat(Stats.BELL_RING);}
            return ((BellBlock) b).onHit(world, state, hit, player, true);
        }
        return false;
    }

    private static boolean findConnectedPulley(Level world, BlockPos pos, Player player, int it, Rotation rot) {
        if (it > 64) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof RopeBlock) {
            return findConnectedPulley(world, pos.above(), player, it + 1, rot);
        } else if (b instanceof PulleyBlock pulley && it != 0) {
            if (world.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
                if (tile.isEmpty() && !player.isShiftKeyDown()) {
                    tile.setDisplayedItem(new ItemStack(ModRegistry.ROPE_ITEM.get()));
                    boolean ret = pulley.windPulley(state, pos, world, rot, null);
                    tile.getDisplayedItem().shrink(1);
                    return ret;
                } else {
                    return pulley.windPulley(state, pos, world, rot, null);
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item i = stack.getItem();

        if (i == this.asItem()) {
            if (hit.getDirection().getAxis() == Direction.Axis.Y || state.getValue(DOWN)) {
                //restores sheared
                if (state.getValue(UP) && !state.getValue(DOWN)) {
                    state = state.setValue(DOWN, true);
                    world.setBlock(pos, state, 0);
                }
                if (addRope(pos.below(), world, player, handIn, this)) {
                    SoundType soundtype = state.getSoundType(world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            return InteractionResult.PASS;
        } else if (stack.isEmpty()) {
            if (state.getValue(UP)) {
                if (ServerConfigs.cached.BELL_CHAIN && findAndRingBell(world, pos, player, 0, s -> s.getBlock() == this))
                    return InteractionResult.sidedSuccess(world.isClientSide);
                else if (findConnectedPulley(world, pos, player, 0, player.isShiftKeyDown() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90)) {
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            if (!player.isShiftKeyDown() && handIn == InteractionHand.MAIN_HAND) {
                if (world.getBlockState(pos.below()).getBlock() == this) {
                    if (removeRope(pos.below(), world, this)) {
                        world.playSound(player, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1, 0.6f);
                        if (!player.getAbilities().instabuild) {
                            Utils.swapItem(player, handIn, stack, new ItemStack(this.asItem()));
                        }
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
        } else if (i instanceof ShearsItem) {
            if (state.getValue(DOWN)) {
                if (!world.isClientSide) {
                    //TODO: proper sound event here
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 0.8F, 1.3F);
                    BlockState newState = state.setValue(DOWN, false).setValue(KNOT, true);
                    world.setBlock(pos, newState, 3);
                    //update below
                    //world.updateNeighborsAt(pos, newState.getBlock());
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }


    public static boolean removeRope(BlockPos pos, Level world, Block ropeBlock) {
        BlockState state = world.getBlockState(pos);
        if (ropeBlock == state.getBlock()) {
            return removeRope(pos.below(), world, ropeBlock);
        } else {
            //if (dist == 0) return false;
            BlockPos up = pos.above();
            if (!(world.getBlockState(up).getBlock() == ropeBlock)) return false;
            FluidState fromFluid = world.getFluidState(up);
            boolean water = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource());
            world.setBlockAndUpdate(up, water ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            tryMove(pos, up, world);
            return true;
        }
    }


    public static boolean addRope(BlockPos pos, Level world, @Nullable Player player, InteractionHand hand, Block ropeBlock) {
        BlockState state = world.getBlockState(pos);
        if (ropeBlock == state.getBlock()) {
            return addRope(pos.below(), world, player, hand, ropeBlock);
        } else {
            return tryPlaceAndMove(player, hand, world, pos, ropeBlock);
        }
    }


    public static boolean tryPlaceAndMove(@Nullable Player player, InteractionHand hand, Level world, BlockPos pos, Block ropeBlock) {
        ItemStack stack = new ItemStack(ropeBlock);

        BlockPlaceContext context = new PlayerLessContext(world, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        if (!context.canPlace()) {
            //checks if block below this is hollow
            BlockPos downPos = pos.below();
            //try move block down
            if (!(world.getBlockState(downPos).getMaterial().isReplaceable()
                    && tryMove(pos, downPos, world))) return false;
            context = new PlayerLessContext(world, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        }

        BlockState state = ItemsUtil.getPlacementState(context, ropeBlock);
        if (state == null) return false;
        if (state == world.getBlockState(context.getClickedPos())) return false;
        if (world.setBlock(context.getClickedPos(), state, 11)) {
            if (player != null) {
                BlockState placedState = world.getBlockState(context.getClickedPos());
                Block block = placedState.getBlock();
                if (block == state.getBlock()) {
                    block.setPlacedBy(world, context.getClickedPos(), placedState, player, stack);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, context.getClickedPos(), stack);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isBlockMovable(BlockState state, Level level, BlockPos pos) {
        return (!state.isAir() && !state.is(Blocks.OBSIDIAN) &&
                !state.is(Blocks.CRYING_OBSIDIAN) && !state.is(Blocks.RESPAWN_ANCHOR))
                && state.getDestroySpeed(level, pos) != -1;
    }

    //TODO: fix order of operations to allow pulling down lanterns
    private static boolean tryMove(BlockPos fromPos, BlockPos toPos, Level world) {
        if (toPos.getY() < world.getMinBuildHeight() || toPos.getY() > world.getMaxBuildHeight()) return false;
        BlockState state = world.getBlockState(fromPos);

        PushReaction push = state.getPistonPushReaction();

        if (isBlockMovable(state, world, fromPos) &&
                (
                        ((push == PushReaction.NORMAL || (toPos.getY() < fromPos.getY() && push == PushReaction.PUSH_ONLY)) && state.canSurvive(world, toPos))
                        || (state.is(ModTags.ROPE_HANG_TAG))
                )
            ) {

            BlockEntity tile = world.getBlockEntity(fromPos);
            if (tile != null) {
                //moves everything if quark is not enabled. bad :/ install quark guys
                if (CompatHandler.quark && !QuarkPistonPlugin.canMoveTile(state)) {
                    return false;
                } else {
                    tile.setRemoved();
                }
            }

            //gets update state for new position

            Fluid fluidState = world.getFluidState(toPos).getType();
            boolean waterFluid = fluidState == Fluids.WATER;
            boolean canHoldWater = false;
            if (state.hasProperty(WATERLOGGED)) {
                canHoldWater = state.is(ModTags.WATER_HOLDER);
                if (!canHoldWater) state = state.setValue(WATERLOGGED, waterFluid);
            } else if (state.getBlock() instanceof AbstractCauldronBlock) {
                if (waterFluid && state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
                    state = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                }
                if (fluidState == Fluids.LAVA && state.is(Blocks.CAULDRON) || state.is(Blocks.LAVA_CAULDRON)) {
                    state = Blocks.LAVA_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                }
            }


            FluidState fromFluid = world.getFluidState(fromPos);
            boolean leaveWater = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource()) && !canHoldWater;
            world.setBlockAndUpdate(fromPos, leaveWater ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());

            //update existing block block to new position
            BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            world.setBlockAndUpdate(toPos, newState);
            if (tile != null) {
                CompoundTag tag = tile.saveWithoutMetadata();
                BlockEntity te = world.getBlockEntity(toPos);
                if(te != null){
                    te.load(tag);
                }
            }
            //world.notifyNeighborsOfStateChange(toPos, state.getBlock());
            world.neighborChanged(toPos, state.getBlock(), toPos);
            return true;
        }
        return false;
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        if (entityIn instanceof Arrow && !worldIn.isClientSide) {
            worldIn.destroyBlock(pos, true, entityIn);
            worldIn.playSound(null, pos, SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 1, 1);
        }
    }


}
