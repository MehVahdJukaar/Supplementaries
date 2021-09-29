package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.PlayerLessContext;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.common.StaticBlockItem;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.RopeChandelierBlock;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPistonPlugin;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
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
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(DISTANCE, 0).setValue(WATERLOGGED, false), VoxelShapes.block());
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
            VoxelShape v = VoxelShapes.empty();
            if (state.getValue(KNOT)) v = VoxelShapes.or(knot);
            if (state.getValue(DOWN)) v = VoxelShapes.or(v, down);
            if (state.getValue(UP)) v = VoxelShapes.or(v, up);
            if (state.getValue(NORTH)) v = VoxelShapes.or(v, north);
            if (state.getValue(SOUTH)) v = VoxelShapes.or(v, south);
            if (state.getValue(WEST)) v = VoxelShapes.or(v, west);
            if (state.getValue(EAST)) v = VoxelShapes.or(v, east);
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED, DISTANCE, KNOT);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return state.getValue(DOWN) && (state.getValue(UP) || entity.position().y() - pos.getY() < (13 / 16f));
    }

    //TODO: make solid when player is not colliding
    private static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 13, 16);

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return ((!state.getValue(UP) && (context.isAbove(COLLISION_SHAPE, pos, true) || !state.getValue(DOWN)))
                || !(context.getEntity() instanceof LivingEntity)) ?
                getShape(state, worldIn, pos, context) : VoxelShapes.empty();

    }

    public static boolean shouldConnectToDir(BlockState thisState, BlockPos currentPos, IWorldReader world, Direction dir) {
        BlockPos facingPos = currentPos.relative(dir);
        return shouldConnectToFace(thisState, world.getBlockState(facingPos), facingPos, dir, world);
    }

    public static boolean shouldConnectToFace(BlockState thisState, BlockState facingState, BlockPos facingPos, Direction dir, IWorldReader world) {
        Block thisBlock = thisState.getBlock();
        Block b = facingState.getBlock();
        boolean isKnot = thisBlock == ModRegistry.ROPE_KNOT.get();
        boolean isVerticalKnot = isKnot && thisState.getValue(RopeKnotBlock.AXIS) == Direction.Axis.Y;

        switch (dir) {
            case UP:
                if (isVerticalKnot) return false;
                return RopeBlock.isSupportingCeiling(facingState, facingPos, world);
            case DOWN:
                if (isVerticalKnot) return false;
                return RopeBlock.isSupportingCeiling(facingPos.above(2), world) || RopeBlock.canConnectDown(facingState);
            default:

                if (b.is(ModRegistry.ROPE_KNOT.get())) {
                    return thisBlock != b && (dir.getAxis() == Direction.Axis.Y || facingState.getValue(RopeKnotBlock.AXIS) == Direction.Axis.Y);
                } else if (isKnot && !isVerticalKnot) {
                    return false;
                }
                return b == ModRegistry.ROPE.get();
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if (!worldIn.isClientSide()) {
            worldIn.getBlockTicks().scheduleTick(currentPos, this, 1);
        }

        if(facing == Direction.UP){
            stateIn = stateIn.setValue(DOWN, shouldConnectToDir(stateIn, currentPos, worldIn, Direction.DOWN));
        }
        stateIn = stateIn.setValue(FACING_TO_PROPERTY_MAP.get(facing), shouldConnectToDir(stateIn, currentPos, worldIn, facing));


        if (facing == Direction.DOWN && !worldIn.isClientSide() && CompatHandler.deco_blocks) {
            RopeChandelierBlock.tryConverting(facingState, worldIn, facingPos);
        }

        return stateIn.setValue(KNOT, hasMiddleKnot(stateIn));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
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
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isClientSide) {
            worldIn.getBlockTicks().scheduleTick(pos, this, 1);
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
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return (this.getDistance(worldIn, pos) < 7);
        //return!(!state.get(UP)&&state.get(NORTH).isNone()&&state.get(SOUTH).isNone()&&state.get(EAST).isNone()&&state.get(WEST).isNone());
    }

    public static boolean isSupportingCeiling(BlockState facingState, BlockPos pos, IWorldReader world) {
        Block b = facingState.getBlock();
        return canSupportCenter(world, pos, Direction.DOWN) || b.is(ModTags.ROPE_SUPPORT_TAG) ||
                (b.is(ModRegistry.ROPE_KNOT.get()) && facingState.getValue(RopeKnotBlock.AXIS) != Direction.Axis.Y);
    }

    public static boolean isSupportingCeiling(BlockPos pos, IWorldReader world) {
        return isSupportingCeiling(world.getBlockState(pos), pos, world);
    }

    public static boolean canConnectDown(BlockPos currentPos, IWorldReader world) {
        BlockState state = world.getBlockState(currentPos.below());
        return canConnectDown(state);
    }

    public static boolean canConnectDown(BlockState downState) {
        Block b = downState.getBlock();
        return (b.is(ModRegistry.ROPE.get()) || b.is(ModTags.ROPE_HANG_TAG)
                || (downState.is(ModRegistry.ROPE_KNOT.get()) && downState.getValue(RopeKnotBlock.AXIS) != Direction.Axis.Y)
                || (downState.hasProperty(HorizontalFaceBlock.FACE) && downState.getValue(HorizontalFaceBlock.FACE) == AttachFace.CEILING)
                || (b instanceof ChainBlock && downState.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y)
                || (downState.hasProperty(BlockStateProperties.HANGING) && downState.getValue(BlockStateProperties.HANGING)));
    }

    public int getDistance(IWorldReader world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutable().move(Direction.UP);
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
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int i = this.getDistance(worldIn, pos);
        BlockState blockstate = state.setValue(DISTANCE, i);
        if (i == 7) {
            worldIn.destroyBlock(pos, true);
        } else if (state != blockstate) {
            worldIn.setBlock(pos, blockstate, 3);
        }
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 60;
    }

    public static boolean findAndRingBell(World world, BlockPos pos, PlayerEntity player, int it, Predicate<BlockState> predicate) {

        if (it > ServerConfigs.cached.BELL_CHAIN_LENGTH) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (predicate.test(state)) {
            return findAndRingBell(world, pos.above(), player, it + 1, predicate);
        } else if (b instanceof BellBlock && it != 0) {
            //boolean success = CommonUtil.tryRingBell(Block b, world, pos, state.getValue(BellBlock.FACING).getClockWise());
            BlockRayTraceResult hit = new BlockRayTraceResult(new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    state.getValue(BellBlock.FACING).getClockWise(), pos, true);
            //if (success && player != null) {//player.awardStat(Stats.BELL_RING);}
            return ((BellBlock) b).onHit(world, state, hit, player, true);
        }
        return false;
    }

    private static boolean findConnectedPulley(World world, BlockPos pos, PlayerEntity player, int it, Rotation rot) {
        if (it > 64) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof RopeBlock) {
            return findConnectedPulley(world, pos.above(), player, it + 1, rot);
        } else if (b instanceof PulleyBlock && it != 0) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof PulleyBlockTile) {
                PulleyBlockTile tile = ((PulleyBlockTile) te);
                if (tile.isEmpty() && !player.isShiftKeyDown()) {
                    tile.setDisplayedItem(new ItemStack(ModRegistry.ROPE_ITEM.get()));
                    boolean ret = ((PulleyBlock) b).axisRotate(state, pos, world, rot, null);
                    tile.getDisplayedItem().shrink(1);
                    return ret;
                } else {
                    return ((PulleyBlock) b).axisRotate(state, pos, world, rot, null);
                }
            }
        }
        return false;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item i = stack.getItem();

        if (i == this.asItem()) {
            if (hit.getDirection().getAxis() == Direction.Axis.Y || state.getValue(DOWN)) {
                //restores sheared
                if(state.getValue(UP) && !state.getValue(DOWN)){
                    state = state.setValue(DOWN, true);
                    world.setBlock(pos, state, 0);
                }
                if (addRope(pos.below(), world, player, handIn, this)) {
                    SoundType soundtype = state.getSoundType(world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (player == null || !player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                    return ActionResultType.sidedSuccess(world.isClientSide);
                }
            }
            return ActionResultType.PASS;
        } else if (stack.isEmpty()) {
            if (state.getValue(UP)) {
                if (ServerConfigs.cached.BELL_CHAIN && findAndRingBell(world, pos, player, 0, s -> s.getBlock() == this))
                    return ActionResultType.sidedSuccess(world.isClientSide);
                else if (findConnectedPulley(world, pos, player, 0, player.isShiftKeyDown() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90)) {
                    return ActionResultType.sidedSuccess(world.isClientSide);
                }
            }
            if (!player.isShiftKeyDown() && handIn == Hand.MAIN_HAND) {
                if (world.getBlockState(pos.below()).getBlock() == this) {
                    if (removeRope(pos.below(), world, this)) {
                        world.playSound(player, pos, SoundEvents.LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 1, 0.6f);
                        if (player == null || !player.abilities.instabuild) {
                            Utils.swapItem(player, handIn, stack, new ItemStack(this.asItem()));
                        }
                        return ActionResultType.sidedSuccess(world.isClientSide);
                    }
                }
            }
        } else if (i instanceof ShearsItem) {
            if (state.getValue(DOWN)) {
                if (!world.isClientSide) {
                    //TODO: proper sound event here
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, player == null ? SoundCategory.BLOCKS : SoundCategory.PLAYERS, 0.8F, 1.3F);
                    BlockState newState = state.setValue(DOWN, false).setValue(KNOT, true);
                    world.setBlock(pos, newState, 3);
                    //update below
                    //world.updateNeighborsAt(pos, newState.getBlock());
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }


    public static boolean removeRope(BlockPos pos, World world, Block ropeBlock) {
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


    public static boolean addRope(BlockPos pos, World world, @Nullable PlayerEntity player, Hand hand, Block ropeBlock) {
        BlockState state = world.getBlockState(pos);
        if (ropeBlock == state.getBlock()) {
            return addRope(pos.below(), world, player, hand, ropeBlock);
        } else {
            return tryPlaceAndMove(player, hand, world, pos, ropeBlock);
        }
    }


    public static boolean tryPlaceAndMove(@Nullable PlayerEntity player, Hand hand, World world, BlockPos pos, Block ropeBlock) {
        ItemStack stack = new ItemStack(ropeBlock);

        BlockItemUseContext context = new PlayerLessContext(world, player, hand, stack, new BlockRayTraceResult(Vector3d.atCenterOf(pos), Direction.UP, pos, false));
        if (!context.canPlace()) {
            //checks if block below this is hollow
            BlockPos downPos = pos.below();
            //try move block down
            if (!(world.getBlockState(downPos).getMaterial().isReplaceable()
                    && tryMove(pos, downPos, world))) return false;
            context = new PlayerLessContext(world, player, hand, stack, new BlockRayTraceResult(Vector3d.atCenterOf(pos), Direction.UP, pos, false));
        }

        BlockState state = StaticBlockItem.getPlacementState(context, ropeBlock);
        if (state == null) return false;
        if (state == world.getBlockState(context.getClickedPos())) return false;
        if (world.setBlock(context.getClickedPos(), state, 11)) {
            if (player != null) {
                BlockState placedState = world.getBlockState(context.getClickedPos());
                Block block = placedState.getBlock();
                if (block == state.getBlock()) {
                    block.setPlacedBy(world, context.getClickedPos(), placedState, player, stack);
                    if (player instanceof ServerPlayerEntity) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, context.getClickedPos(), stack);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isObsidian(BlockState state) {
        return (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.RESPAWN_ANCHOR));
    }

    //TODO: fix order of operations to allow pulling down lanterns
    private static boolean tryMove(BlockPos fromPos, BlockPos toPos, World world) {
        if (toPos.getY() < 0 || toPos.getY() > 255) return false;
        BlockState state = world.getBlockState(fromPos);
        Block block = state.getBlock();
        PushReaction push = state.getPistonPushReaction();

        if ((push == PushReaction.NORMAL || (toPos.getY() < fromPos.getY() && push == PushReaction.PUSH_ONLY) || block.is(ModTags.ROPE_HANG_TAG)) && state.getDestroySpeed(world, fromPos) != -1
                && state.canSurvive(world, toPos) && !block.isAir(state, world, fromPos) && !isObsidian(state)) {

            TileEntity tile = world.getBlockEntity(fromPos);
            if (tile != null) {
                //moves everything if quark is not enabled. bad :/ install quark guys
                if (CompatHandler.quark && !QuarkPistonPlugin.canMoveTile(state)) {
                    return false;
                } else {
                    tile.setRemoved();
                }
            }

            //gets update state for new position

            boolean toFluid = world.getFluidState(toPos).getType() == Fluids.WATER;
            boolean canHoldWater = false;
            if (state.hasProperty(WATERLOGGED)) {
                canHoldWater = state.is(ModTags.WATER_HOLDER);
                if (!canHoldWater) state = state.setValue(WATERLOGGED, toFluid);
            }
            if (state.getBlock() instanceof CauldronBlock && toFluid) state = state.setValue(CauldronBlock.LEVEL, 3);


            FluidState fromFluid = world.getFluidState(fromPos);
            boolean leaveWater = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource()) && !canHoldWater;
            world.setBlockAndUpdate(fromPos, leaveWater ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());

            //update existing block block to new position
            BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            world.setBlockAndUpdate(toPos, newState);
            if (tile != null) {
                tile.setPosition(toPos);
                TileEntity target = TileEntity.loadStatic(newState, tile.save(new CompoundNBT()));
                if (target != null) {
                    world.setBlockEntity(toPos, target);
                    target.clearCache();
                }
            }
            //world.notifyNeighborsOfStateChange(toPos, state.getBlock());
            world.neighborChanged(toPos, state.getBlock(), toPos);
            return true;
        }
        return false;
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        if (entityIn instanceof ArrowEntity && !worldIn.isClientSide) {
            worldIn.destroyBlock(pos, true, entityIn);
            worldIn.playSound(null, pos, SoundEvents.LEASH_KNOT_BREAK, SoundCategory.BLOCKS, 1, 1);
        }
    }



}
